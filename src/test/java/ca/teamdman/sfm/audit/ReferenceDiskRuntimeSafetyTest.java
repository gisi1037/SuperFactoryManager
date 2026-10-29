package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.gui.ContainerManager;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.program.ProgramEditService;
import ca.teamdman.sfm.common.program.ProgramEditSnapshot;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Staged references are inert media until an explicitly authorized asynchronous activation path exists. */
class ReferenceDiskRuntimeSafetyTest {

    private static final String COUNTER = "STATE $count = 0 EVERY 1 TICKS DO SET $count = $count + 1 END";

    @Test
    void insertionAndNbtSyncPreserveReferenceAndInvalidMediaWithoutCompilation() throws Exception {
        for (boolean invalid : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            ItemStack disk = fixture.manager.getDisk()
                .copy();
            reference(disk, invalid);
            NBTTagCompound original = (NBTTagCompound) disk.getTagCompound()
                .copy();
            fixture.manager.setInventorySlotContents(0, disk);
            assertInert(fixture.manager);
            assertEquals(original, disk.getTagCompound());
            fixture.manager.setRunning(true);
            fixture.manager.recompile(COUNTER);
            fixture.manager.updateEntity();
            fixture.manager.runOnceWithLogLevel(LogLevel.DEBUG);
            assertInert(fixture.manager);
            assertEquals(original, disk.getTagCompound());
            NBTTagCompound saved = new NBTTagCompound();
            fixture.manager.writeToNBT(saved);
            assertEquals(fixture.manager.isRunning(), saved.getBoolean("isRunning"));
            TileEntityManager loaded = new TileEntityManager();
            loaded.readFromNBT(saved);
            assertInert(loaded);
            assertEquals(
                original,
                loaded.getDisk()
                    .getTagCompound());
            // Tile sync invokes the same NBT reader on the client; it must not resolve/compile an empty source.
            loaded.onDataPacket(null, new S35PacketUpdateTileEntity(1, 2, 3, 1, saved));
            assertInert(loaded);
            assertEquals(
                original,
                loaded.getDisk()
                    .getTagCompound());
        }
    }

    @Test
    void blockDescriptionRedactsAuthorityFromACopyWhileSavesAndExtractionKeepThePhysicalDisk() throws Exception {
        for (boolean invalid : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            ItemStack disk = fixture.manager.getDisk()
                .copy();
            reference(disk, invalid);
            if (invalid) disk.getTagCompound()
                .setString(ItemDisk.NBT_PROGRAM, "PRIVATE_SOURCE_SENTINEL");
            NBTTagCompound original = (NBTTagCompound) disk.getTagCompound()
                .copy();
            fixture.manager.setInventorySlotContents(0, disk);
            S35PacketUpdateTileEntity packet = (S35PacketUpdateTileEntity) fixture.manager.getDescriptionPacket();
            NBTTagCompound preview = packet.func_148857_g()
                .getCompoundTag("disk")
                .getCompoundTag("tag");
            assertTrue(preview.hasKey(ProgramDiskData.NBT_REFERENCE, 10));
            assertTrue(
                preview.getCompoundTag(ProgramDiskData.NBT_REFERENCE)
                    .hasNoTags());
            assertFalse(preview.hasKey(ItemDisk.NBT_PROGRAM));
            assertFalse(
                packet.func_148857_g()
                    .toString()
                    .contains(repeat('a')));
            assertFalse(
                packet.func_148857_g()
                    .toString()
                    .contains(repeat('b')));
            assertFalse(
                packet.func_148857_g()
                    .toString()
                    .contains("PRIVATE_SOURCE_SENTINEL"));
            assertEquals(original, disk.getTagCompound(), "Packet redaction must not mutate the installed disk");
            TileEntityManager client = new TileEntityManager();
            client.onDataPacket(null, packet);
            assertEquals(ProgramDiskData.Mode.INVALID, ProgramDiskData.mode(client.getDisk()));
            assertInert(client);
            client.setInventorySlotContents(0, disk.copy());
            assertEquals(
                fixture.manager.isRunning(),
                client.isRunning(),
                "Authorized slot synchronization must reveal the server intent retained behind the redacted mirror");
            assertFalse(client.hasCompiledProgram(), "Inventory metadata alone never grants a client executable");
            NBTTagCompound saved = new NBTTagCompound();
            fixture.manager.writeToNBT(saved);
            assertEquals(
                original,
                saved.getCompoundTag("disk")
                    .getCompoundTag("tag"));
            ItemStack extracted = fixture.manager.decrStackSize(0, 1);
            assertEquals(original, extracted.getTagCompound());
            assertEquals(
                repeat('a'),
                extracted.getTagCompound()
                    .getCompoundTag(ProgramDiskData.NBT_REFERENCE)
                    .getString("grant"));
        }
    }

    @Test
    void changingLiveStatelessMediaCannotKeepTheOldExecutableOrTickIt() throws Exception {
        Fixture fixture = new Fixture();
        fixture.manager.saveCodeAndRecompile("EVERY 1 TICKS DO PRINT(1) END");
        assertNotNull(fixture.manager.getCompiledProgram());
        assertNull(fixture.manager.getProgramState());
        long tick = fixture.manager.getManagerTick();
        reference(fixture.manager.getDisk(), false);
        assertNull(fixture.manager.getCompiledProgram());
        assertFalse(fixture.manager.hasCompiledProgram());
        fixture.manager.updateEntity();
        fixture.manager.runOnceWithLogLevel(LogLevel.DEBUG);
        assertEquals(tick, fixture.manager.getManagerTick());
        assertInert(fixture.manager);
    }

    @Test
    void liveReferenceMutationFlushesCommittedOldSchemaStateBeforeDiscardingItsStore() throws Exception {
        Fixture fixture = new Fixture();
        fixture.manager.saveCodeAndRecompile(COUNTER);
        fixture.manager.updateEntity();
        fixture.manager.updateEntity();
        ProgramStateStore old = fixture.manager.getProgramState();
        assertTrue(old.isDirty());
        assertEquals(
            2,
            old.read(0)
                .asLong());
        reference(fixture.manager.getDisk(), false);
        fixture.manager.recompile("");
        assertFalse(old.isDirty(), "The accepted old-schema values must publish before its store is dropped");
        assertNull(fixture.manager.getProgramState());
        NBTTagCompound entry = fixture.manager.getDisk()
            .getTagCompound()
            .getCompoundTag(ProgramStateStore.NBT_KEY)
            .getTagList("entries", 10)
            .getCompoundTagAt(0);
        assertEquals(
            2,
            entry.getCompoundTag("value")
                .getLong("value"));
        assertInert(fixture.manager);
    }

    @Test
    void stateOwnershipConflictPausesAndRetainsUnpublishedHeapValues() throws Exception {
        Fixture fixture = new Fixture();
        fixture.manager.saveCodeAndRecompile(COUNTER);
        fixture.manager.updateEntity();
        ProgramStateStore old = fixture.manager.getProgramState();
        reference(fixture.manager.getDisk(), false);
        NBTTagCompound root = fixture.manager.getDisk()
            .getTagCompound();
        NBTBase replacement = root.getTag(ProgramStateStore.NBT_KEY)
            .copy();
        root.setTag(ProgramStateStore.NBT_KEY, replacement);
        fixture.manager.recompile(COUNTER);
        assertInert(fixture.manager);
        assertSame(old, fixture.manager.getProgramState());
        assertTrue(old.isDirty());
        assertEquals(
            1,
            old.read(0)
                .asLong());
        assertSame(replacement, root.getTag(ProgramStateStore.NBT_KEY));
        assertTrue(
            fixture.manager.getStatusMessage()
                .contains("Persistent state conflict"));
        assertThrows(IllegalStateException.class, () -> fixture.manager.writeToNBT(new NBTTagCompound()));
    }

    @Test
    void sourceEditingSavingAndResetCannotConvertReferencesOrInvalidMedia() throws Exception {
        for (boolean invalid : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            fixture.manager.saveCodeAndRecompile(COUNTER);
            ProgramEditSnapshot expected = ProgramEditService.open(fixture.container);
            reference(fixture.manager.getDisk(), invalid);
            NBTTagCompound original = (NBTTagCompound) fixture.manager.getDisk()
                .getTagCompound()
                .copy();
            assertThrows(IllegalStateException.class, () -> ProgramEditService.open(fixture.container));
            assertFalse(ProgramEditService.save(fixture.container, expected, "").accepted);
            assertNotNull(fixture.manager.trySaveProgram(""));
            fixture.manager.saveCodeAndRecompile("");
            fixture.manager.resetProgramState();
            fixture.manager.reset();
            fixture.manager.recompile(COUNTER);
            assertEquals(
                original,
                fixture.manager.getDisk()
                    .getTagCompound());
            assertInert(fixture.manager);
        }
    }

    private static void reference(ItemStack disk, boolean invalid) {
        if (!disk.hasTagCompound()) disk.setTagCompound(new NBTTagCompound());
        NBTTagCompound root = disk.getTagCompound();
        root.removeTag(ItemDisk.NBT_PROGRAM);
        NBTTagCompound reference = new NBTTagCompound();
        reference.setInteger("format", 1);
        reference.setString("grant", repeat('a'));
        reference.setString("digest", repeat('b'));
        if (invalid) reference.setString("unexpected", "preserve malformed evidence");
        root.setTag(ProgramDiskData.NBT_REFERENCE, reference);
    }

    private static String repeat(char value) {
        return new String(new char[64]).replace('\0', value);
    }

    private static void assertInert(TileEntityManager manager) {
        assertNull(manager.getCompiledProgram());
        assertFalse(manager.hasCompiledProgram());
        if (ProgramDiskData.mode(manager.getDisk()) == ProgramDiskData.Mode.INVALID) assertFalse(manager.isRunning());
        assertNotNull(manager.getDisk());
        assertNotEquals(ProgramDiskData.Mode.LEGACY, ProgramDiskData.mode(manager.getDisk()));
    }

    private static final class Fixture {

        final AuditRegressionTest.Fixture world = new AuditRegressionTest.Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(world);
        final ContainerManager container;

        Fixture() throws Exception {
            AuditRegressionTest.FakePlayer player = AuditRegressionTest.allocate(AuditRegressionTest.FakePlayer.class);
            player.worldObj = world.world;
            player.inventory = new InventoryPlayer(player);
            container = new ContainerManager(player.inventory, manager);
        }
    }
}
