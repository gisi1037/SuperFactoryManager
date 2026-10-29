package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.command.CommandSfmFluidRecovery;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.recovery.FluidRecovery;
import ca.teamdman.sfm.common.recovery.ItemRecovery;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.gameevent.TickEvent;

class FluidRecoveryActionsTest {

    @BeforeEach
    void serverThread() {
        ServerUiTasks.clear();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
    }

    @AfterEach
    void clearThread() {
        ServerUiTasks.clear();
    }

    private TileEntityManager uncertain(Fixture fixture) throws Exception {
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        FluidStack evidence = water(1000);
        evidence.tag = new NBTTagCompound();
        evidence.tag.setString("owner", "original evidence");
        NBTTagCompound record = manager
            .preserveFluid(evidence, SRC, ForgeDirection.NORTH, true, "destination insertion");
        ItemRecovery.setTarget(record, DST, -1, ForgeDirection.EAST, -1);
        return manager;
    }

    private NBTTagCompound record(TileEntityManager manager) {
        return manager.getDisk()
            .getTagCompound()
            .getCompoundTag(FluidRecovery.KEY);
    }

    private String id(TileEntityManager manager) {
        return FluidRecovery.inspect(manager)
            .getString("recoveryId");
    }

    private TileEntityManager reload(Fixture fixture, TileEntityManager manager) throws Exception {
        NBTTagCompound saved = new NBTTagCompound();
        manager.writeToNBT(saved);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(saved, bytes);
        NBTTagCompound decoded = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
        TileEntityManager restored = new TileEntityManager();
        restored.setWorldObj(fixture.world);
        restored.readFromNBT(decoded);
        fixture.world.tiles.put(MANAGER, restored);
        return restored;
    }

    @Test
    void realNbtReloadKeepsEvidenceThenAcknowledgementKeepsSingleInactiveReceiptAndPause() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = uncertain(fixture);
        String id = id(manager);
        NBTTagCompound before = (NBTTagCompound) record(manager).copy();
        manager = reload(fixture, manager);
        assertEquals(before, record(manager));
        assertEquals(id, id(manager));
        fixture.world.time = 93;
        manager.setRunning(true);
        FluidRecovery.resolvePresent(manager, id, "operator");
        assertFalse(manager.hasPendingRecovery());
        assertFalse(manager.isRunning());
        assertTrue(
            manager.getStatusMessage()
                .contains("Fluid recovery resolved"));
        NBTTagCompound receipt = manager.getDisk()
            .getTagCompound()
            .getCompoundTag(FluidRecovery.LAST_KEY);
        assertEquals("resolved-present", receipt.getString("state"));
        assertEquals("operator", receipt.getString("actor"));
        assertEquals(93, receipt.getLong("resolvedAt"));
        assertEquals(1000, receipt.getInteger("Amount"));
        assertFalse(receipt.hasKey("Tag"));
        manager = reload(fixture, manager);
        assertFalse(manager.hasPendingRecovery());
        assertFalse(manager.isRunning());
        assertEquals(
            receipt,
            manager.getDisk()
                .getTagCompound()
                .getCompoundTag(FluidRecovery.LAST_KEY));
        TileEntityManager loaded = manager;
        assertThrows(IllegalStateException.class, () -> FluidRecovery.resolvePresent(loaded, id, "again"));
        String oldId = id;
        manager.preserveFluid(water(77), SRC, ForgeDirection.DOWN, true, "source rollback");
        String newId = id(manager);
        assertNotEquals(oldId, newId);
        assertThrows(IllegalStateException.class, () -> FluidRecovery.resolvePresent(loaded, oldId, "stale"));
        FluidRecovery.resolvePresent(manager, newId, "operator2");
        NBTTagCompound last = manager.getDisk()
            .getTagCompound()
            .getCompoundTag(FluidRecovery.LAST_KEY);
        assertEquals(newId, last.getString("recoveryId"));
        assertEquals(77, last.getInteger("Amount"));
        assertFalse(last.hasKey(FluidRecovery.LAST_KEY));
    }

    @Test
    void inspectReturnsCopyAndNeitherActionTouchesActualOrUnloadedTanks() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = uncertain(fixture);
        TankTile neverCall = new TankTile(400) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                throw new AssertionError("operator workflow called fill");
            }

            @Override
            public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                throw new AssertionError("operator workflow called drain");
            }

            @Override
            public FluidStack drain(ForgeDirection side, int amount, boolean actual) {
                throw new AssertionError("operator workflow called drain");
            }
        };
        fixture.world.tiles.put(SRC, neverCall);
        fixture.world.tiles.put(DST, neverCall);
        fixture.world.unloaded.add(SRC);
        fixture.world.unloaded.add(DST);
        NBTTagCompound snapshot = FluidRecovery.inspect(manager);
        String id = snapshot.getString("recoveryId");
        snapshot.setInteger("Amount", 1);
        snapshot.getCompoundTag("Tag")
            .setString("owner", "changed");
        assertEquals(1000, record(manager).getInteger("Amount"));
        assertEquals(
            "original evidence",
            record(manager).getCompoundTag("Tag")
                .getString("owner"));
        FluidRecovery.resolvePresent(manager, id, "operator");
        assertEquals(400, neverCall.amount);
        assertEquals(0, count(fixture.source));
        assertEquals(0, count(fixture.dest));
    }

    @Test
    void knownPendingIsNeverDiscardedAndPacedReturnPolicyIsUnchanged() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = uncertain(fixture);
        NBTTagCompound record = record(manager);
        record.setString("state", "pending");
        record.setBoolean("recoveryBlocked", false);
        String id = id(manager);
        NBTTagCompound before = (NBTTagCompound) record.copy();
        assertThrows(IllegalStateException.class, () -> FluidRecovery.resolvePresent(manager, id, "operator"));
        assertEquals(before, record(manager));
        TankTile source = new TankTile(0);
        fixture.world.tiles.put(SRC, source);
        fixture.world.time = 21; // Existing automatic return remains paced to a multiple of 20.
        manager.updateEntity();
        assertEquals(0, source.amount);
        assertTrue(manager.hasPendingRecovery());
        assertEquals(before, record(manager));
        assertFalse(manager.isRunning());
        assertFalse(
            manager.getDisk()
                .getTagCompound()
                .hasKey(FluidRecovery.LAST_KEY));
    }

    @Test
    void corruptUnknownLegacyAndConflictingRecordsSurviveInspectionReloadAndRefusedAcknowledgement() throws Exception {
        List<Consumer<NBTTagCompound>> corruptions = Arrays.asList(
            r -> r.setInteger("version", 99),
            r -> r.setString("version", "1"),
            r -> r.removeTag("version"),
            r -> r.setString("state", "future"),
            r -> r.setInteger("state", 1),
            r -> r.removeTag("state"),
            r -> r.setString("recoveryId", "bad-id"),
            r -> r.removeTag("recoveryId"),
            r -> r.setInteger("Amount", 0),
            r -> r.setString("Amount", "1000"),
            r -> r.setString("FluidName", ""),
            r -> r.removeTag("sourceX"),
            r -> r.setInteger("sourceSide", 6),
            r -> r.setInteger("targetSide", -1),
            r -> r.setString("Tag", "bad tag"),
            r -> r.setString("recoveryBlocked", "true"),
            r -> r.removeTag("stage"));
        for (Consumer<NBTTagCompound> corruption : corruptions) {
            Fixture fixture = new Fixture();
            TileEntityManager manager = uncertain(fixture);
            String id = id(manager);
            corruption.accept(record(manager));
            NBTTagCompound before = (NBTTagCompound) record(manager).copy();
            assertEquals(before, FluidRecovery.inspect(manager));
            manager = reload(fixture, manager);
            TileEntityManager restored = manager;
            assertThrows(IllegalStateException.class, () -> FluidRecovery.resolvePresent(restored, id, "operator"));
            assertEquals(before, record(manager));
            assertTrue(manager.hasPendingRecovery());
            assertFalse(manager.isRunning());
        }
        Fixture fixture = new Fixture();
        TileEntityManager manager = uncertain(fixture);
        String id = id(manager);
        manager.getDisk()
            .getTagCompound()
            .setString(ItemRecovery.KEY, "corrupt item evidence");
        NBTTagCompound before = (NBTTagCompound) manager.getDisk()
            .getTagCompound()
            .copy();
        assertThrows(IllegalStateException.class, () -> FluidRecovery.resolvePresent(manager, id, "operator"));
        assertEquals(
            before,
            manager.getDisk()
                .getTagCompound());
        manager.getDisk()
            .getTagCompound()
            .setString(FluidRecovery.KEY, "corrupt fluid evidence");
        before = (NBTTagCompound) manager.getDisk()
            .getTagCompound()
            .copy();
        assertThrows(IllegalStateException.class, () -> FluidRecovery.inspect(manager));
        assertThrows(IllegalStateException.class, () -> FluidRecovery.resolvePresent(manager, id, "operator"));
        assertEquals(
            before,
            manager.getDisk()
                .getTagCompound());
    }

    @Test
    void receiptHasBoundedStringsAndDoesNotRetainArbitraryPayload() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = uncertain(fixture);
        String id = id(manager);
        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < 8192; i++) huge.append('x');
        record(manager).setString("futureDiagnostic", huge.toString());
        record(manager).getCompoundTag("Tag")
            .setString("largePayload", huge.toString());
        FluidRecovery.resolvePresent(manager, id, huge.toString());
        NBTTagCompound receipt = manager.getDisk()
            .getTagCompound()
            .getCompoundTag(FluidRecovery.LAST_KEY);
        assertEquals(
            64,
            receipt.getString("actor")
                .length());
        assertFalse(receipt.hasKey("futureDiagnostic"));
        assertFalse(receipt.hasKey("Tag"));
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(receipt, encoded);
        assertTrue(encoded.size() < 2048);
    }

    @Test
    void loadedServerManagerBoundaryRejectsUnloadedReplacedInvalidAndClientManagers() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = uncertain(fixture);
        String id = id(manager);
        fixture.world.unloaded.add(MANAGER);
        assertThrows(IllegalStateException.class, () -> FluidRecovery.inspect(manager));
        assertThrows(IllegalStateException.class, () -> FluidRecovery.resolvePresent(manager, id, "operator"));
        fixture.world.unloaded.remove(MANAGER);
        fixture.world.tiles.remove(MANAGER);
        assertThrows(IllegalStateException.class, () -> FluidRecovery.inspect(manager));
        fixture.world.tiles.put(MANAGER, manager);
        fixture.world.isRemote = true;
        assertThrows(IllegalStateException.class, () -> FluidRecovery.inspect(manager));
        fixture.world.isRemote = false;
        manager.invalidate();
        assertThrows(IllegalStateException.class, () -> FluidRecovery.inspect(manager));
        assertTrue(manager.hasPendingRecovery());
    }

    @Test
    void commandRequiresPermissionTwoServerThreadLoadedCurrentWorldAndExactSyntax() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = uncertain(fixture);
        CommandSfmFluidRecovery command = new CommandSfmFluidRecovery();
        Sender sender = new Sender(fixture.world);
        assertEquals(2, command.getRequiredPermissionLevel());
        assertFalse(command.canCommandSenderUseCommand(sender));
        sender.allowWorld = false;
        assertThrows(CommandException.class, () -> command.processCommand(sender, args("inspect")));
        sender.permission = 2;
        assertTrue(command.canCommandSenderUseCommand(sender));
        AtomicReference<Throwable> result = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                command.processCommand(sender, args("inspect"));
            } catch (Throwable failure) {
                result.set(failure);
            }
        });
        worker.start();
        worker.join();
        assertTrue(result.get() instanceof CommandException);
        assertTrue(
            result.get()
                .getMessage()
                .contains("server thread"));
        assertThrows(WrongUsageException.class, () -> command.processCommand(sender, args("claim")));
        assertThrows(WrongUsageException.class, () -> command.processCommand(sender, new String[] { "0", "64", "0" }));
        sender.allowWorld = true;
        command.processCommand(sender, args("inspect"));
        assertTrue(sender.message.contains(id(manager)));
        assertTrue(sender.message.contains("evidence amount=1000"));
        assertTrue(sender.message.contains("dimension -1"));
        fixture.world.unloaded.add(MANAGER);
        assertThrows(CommandException.class, () -> command.processCommand(sender, args("inspect")));
        fixture.world.unloaded.remove(MANAGER);
        String id = id(manager);
        command.processCommand(sender, new String[] { "0", "64", "0", "resolve-present", id });
        assertFalse(manager.hasPendingRecovery());
        assertTrue(sender.message.contains("no fluid moved or created"));
        assertThrows(
            CommandException.class,
            () -> command.processCommand(sender, new String[] { "0", "64", "0", "resolve-present", id }));
    }

    @Test
    void helperRejectsOffThreadBeforeReadingManagerWorld() throws Exception {
        TileEntityManager forbidden = new TileEntityManager() {

            @Override
            public World getWorldObj() {
                throw new AssertionError("world access before server-thread guard");
            }
        };
        ServerUiTasks.clear();
        assertThrows(IllegalStateException.class, () -> FluidRecovery.inspect(forbidden));
        assertThrows(IllegalStateException.class, () -> FluidRecovery.resolvePresent(forbidden, "id", "operator"));
    }

    private String[] args(String action) {
        return new String[] { "0", "64", "0", action };
    }

    private static final class Sender implements ICommandSender {

        final World world;
        int permission = 1;
        boolean allowWorld = true;
        String message;

        Sender(World world) {
            this.world = world;
        }

        @Override
        public String getCommandSenderName() {
            return "fixture operator";
        }

        @Override
        public IChatComponent func_145748_c_() {
            return new ChatComponentText(getCommandSenderName());
        }

        @Override
        public void addChatMessage(IChatComponent value) {
            message = value.getUnformattedText();
        }

        @Override
        public boolean canCommandSenderUseCommand(int level, String name) {
            return level <= permission;
        }

        @Override
        public ChunkCoordinates getPlayerCoordinates() {
            return new ChunkCoordinates(0, 64, 0);
        }

        @Override
        public World getEntityWorld() {
            assertTrue(allowWorld, "command accessed world before permission/thread check");
            return world;
        }
    }
}
