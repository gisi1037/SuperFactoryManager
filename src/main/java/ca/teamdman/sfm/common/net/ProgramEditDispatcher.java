package ca.teamdman.sfm.common.net;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.entity.player.EntityPlayerMP;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramEditService;
import ca.teamdman.sfm.common.program.ProgramEditSnapshot;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.ProgramRevision;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Netty handlers only enqueue; world access and edits run at a bounded server-tick boundary. */
public final class ProgramEditDispatcher {

    public static final ProgramEditDispatcher INSTANCE = new ProgramEditDispatcher();
    private static final ConcurrentLinkedQueue<Pending> QUEUE = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<UUID, Boolean> PLAYERS = new ConcurrentHashMap<>();
    private static final AtomicInteger COUNT = new AtomicInteger();

    private ProgramEditDispatcher() {}

    public static void enqueue(EntityPlayerMP player, PacketSaveProgram packet) {
        UUID id = player.getUniqueID();
        if (PLAYERS.putIfAbsent(id, Boolean.TRUE) != null) return;
        if (COUNT.incrementAndGet() > 64) {
            COUNT.decrementAndGet();
            PLAYERS.remove(id);
            return;
        }
        QUEUE.add(new Pending(player, packet, id));
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        for (int i = 0; i < 2; i++) {
            Pending pending = QUEUE.poll();
            if (pending == null) break;
            try {
                process(pending.player, pending.packet);
            } catch (RuntimeException error) {
                SuperFactoryManager.LOG.error("Program edit failed without confirmation", error);
            } finally {
                PLAYERS.remove(pending.id);
                COUNT.decrementAndGet();
            }
        }
    }

    public static void clear() {
        QUEUE.clear();
        PLAYERS.clear();
        COUNT.set(0);
    }

    private static void process(EntityPlayerMP player, PacketSaveProgram packet) {
        TileEntityManager manager = ManagerAccess.get(player, packet.x, packet.y, packet.z);
        if (manager == null) return;
        ManagerEditContext container = (ManagerEditContext) player.openContainer;
        PacketProgramEditReply cached = container.getProgramEditReply(packet.requestId);
        if (cached != null) {
            SFMNetwork.WRAPPER.sendTo(cached, player);
            return;
        }
        ProgramEditSnapshot snapshot = null;
        boolean accepted = false;
        String message;
        try {
            if (packet.requestId <= container.getLastProgramEditRequest())
                throw new IllegalStateException("Stale program request");
            if (!packet.save) {
                snapshot = ProgramEditService.open(container);
                accepted = true;
                message = "Program loaded";
            } else {
                ca.teamdman.sfm.common.program.ProgramDiskData.requireLegacy(manager.getDisk());
                String current = ItemDisk.getProgramString(manager.getDisk());
                byte[] digest = packet.expectedDigest != null ? packet.expectedDigest
                    : ProgramRevision.digest(packet.expected.source);
                if (!ProgramRevision.matches(digest, current))
                    throw new IllegalStateException("Program changed. Keep your draft and reopen the editor.");
                ProgramEditSnapshot expected = new ProgramEditSnapshot(
                    packet.expected.sessionId,
                    packet.expected.bindingGeneration,
                    packet.expected.documentId,
                    packet.expected.revision,
                    current);
                ProgramEditService.SaveResult result = ProgramEditService.save(container, expected, packet.code);
                snapshot = result.snapshot;
                accepted = result.accepted;
                message = result.message;
            }
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException error) {
            message = error.getMessage();
        }
        PacketProgramEditReply reply = new PacketProgramEditReply(
            packet.x,
            packet.y,
            packet.z,
            player.openContainer.windowId,
            packet.requestId,
            accepted,
            snapshot,
            message);
        container.rememberProgramEditReply(reply);
        SFMNetwork.WRAPPER.sendTo(reply, player);
        if (accepted) player.worldObj.markBlockForUpdate(packet.x, packet.y, packet.z);
    }

    private static final class Pending {

        final EntityPlayerMP player;
        final PacketSaveProgram packet;
        final UUID id;

        Pending(EntityPlayerMP player, PacketSaveProgram packet, UUID id) {
            this.player = player;
            this.packet = packet;
            this.id = id;
        }
    }
}
