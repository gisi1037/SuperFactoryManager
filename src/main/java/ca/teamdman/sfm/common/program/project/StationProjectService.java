package ca.teamdman.sfm.common.program.project;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.event.world.WorldEvent;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.net.PacketStationRequest;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Container;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Live authority stays on the server thread; the world-owned worker only sees immutable drafts. */
public final class StationProjectService {

    public static final StationProjectService INSTANCE = new StationProjectService();
    private ProjectWorkspace workspace;
    private WorldServer world;

    private StationProjectService() {}

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || workspace == null) return;
        try {
            workspace.drainCallbacks(8);
        } catch (RuntimeException error) {
            SuperFactoryManager.LOG.error("Station completion failed; reopen to reconcile the saved revision", error);
        }
    }

    @SubscribeEvent
    public void unload(WorldEvent.Unload event) {
        if (event.world == world) close();
    }

    public void close() {
        if (workspace != null) workspace.close();
        workspace = null;
        world = null;
    }

    private ProjectWorkspace workspace() {
        WorldServer overworld = MinecraftServer.getServer()
            .worldServerForDimension(0);
        if (workspace != null && world != overworld) close();
        if (workspace == null) {
            if (overworld == null || !(overworld.getSaveHandler() instanceof SaveHandler))
                throw new IllegalStateException("World project storage is unavailable");
            world = overworld;
            workspace = new ProjectWorkspace(
                ((SaveHandler) world.getSaveHandler()).getWorldDirectory()
                    .toPath());
        }
        return workspace;
    }

    public static ProgrammingStationMui2Container authorized(EntityPlayerMP player, PacketStationRequest request) {
        if (!ServerUiTasks.isServerThread() || !ca.teamdman.sfm.common.net.PlayerUiAccess.isLive(player)
            || !(player.openContainer instanceof ProgrammingStationMui2Container)) return null;
        ProgrammingStationMui2Container container = (ProgrammingStationMui2Container) player.openContainer;
        return container.windowId == request.windowId && container.sessionId.equals(request.sessionId)
            && container.station.xCoord == request.x
            && container.station.yCoord == request.y
            && container.station.zCoord == request.z
            && container.station.isUsableBy(player) ? container : null;
    }

    public void process(EntityPlayerMP player, PacketStationRequest request) {
        ProgrammingStationMui2Container container = authorized(player, request);
        if (container == null) return;
        if (container.lastReply != null && container.lastReply.requestId == request.requestId) {
            SFMNetwork.WRAPPER.sendTo(container.lastReply, player);
            return;
        }
        if (container.pendingRequestId == request.requestId) return;
        if (request.requestId <= container.lastRequestId || container.pendingRequestId != 0) {
            SFMNetwork.WRAPPER.sendTo(
                new PacketStationReply(request, false, "Stale request or another project request is pending"),
                player);
            return;
        }
        container.lastRequestId = request.requestId;
        container.pendingRequestId = request.requestId;
        try {
            request.validate();
            ProjectWorkspace store = workspace();
            UUID owner = player.getUniqueID();
            boolean admitted;
            switch (request.action) {
                case CREATE:
                    admitted = store.create(
                        owner,
                        request.name,
                        request.source,
                        result -> finishDraft(player, container, request, result));
                    break;
                case LOAD:
                    admitted = store
                        .load(owner, request.projectId, result -> finishDraft(player, container, request, result));
                    break;
                case SAVE:
                    admitted = store.save(
                        owner,
                        request.projectId,
                        request.expectedRevision,
                        request.name,
                        request.source,
                        result -> finishDraft(player, container, request, result));
                    break;
                case VALIDATE:
                    admitted = store.validate(owner, request.projectId, request.expectedRevision, result -> {
                        PacketStationReply reply = reply(request, result);
                        reply.validation = result.value;
                        finish(player, container, request, reply);
                    });
                    break;
                default:
                    admitted = store.list(owner, request.offset, result -> {
                        PacketStationReply reply = reply(request, result);
                        reply.page = result.value;
                        finish(player, container, request, reply);
                    });
            }
            if (!admitted) finish(
                player,
                container,
                request,
                new PacketStationReply(request, false, "Project storage is busy or closing; draft kept"));
        } catch (IllegalArgumentException | IllegalStateException error) {
            finish(player, container, request, new PacketStationReply(request, false, error.getMessage()));
        }
    }

    private static PacketStationReply reply(PacketStationRequest request, ProjectResult<?> result) {
        return new PacketStationReply(
            request,
            result.isSuccess(),
            result.isSuccess() ? "Project request completed" : result.message);
    }

    private static void finishDraft(EntityPlayerMP player, ProgrammingStationMui2Container container,
        PacketStationRequest request, ProjectResult<ProjectDraft> result) {
        PacketStationReply reply = reply(request, result);
        reply.draft = result.value;
        // An admitted save may finish after closure, but it must not rebind a stale block/UI.
        if (result.isSuccess() && authorized(player, request) == container)
            container.station.selectProject(result.value.id);
        finish(player, container, request, reply);
    }

    private static void finish(EntityPlayerMP player, ProgrammingStationMui2Container container,
        PacketStationRequest request, PacketStationReply reply) {
        container.pendingRequestId = 0;
        container.lastReply = reply;
        if (authorized(player, request) == container) SFMNetwork.WRAPPER.sendTo(reply, player);
    }
}
