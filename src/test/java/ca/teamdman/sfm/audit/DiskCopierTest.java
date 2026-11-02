package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.Config;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.program.DiskCopyPlan;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.program.ProgramDocument;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.TileEntityDiskCopier;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.gameevent.TickEvent;

class DiskCopierTest {

    private Item previous;
    private int previousTicks, previousBatch;
    private static final BlockPos POS = new BlockPos(0, 64, 0);

    @BeforeEach
    void setup() {
        previous = ModItems.disk;
        previousTicks = Config.diskCopierTicksPerCycle;
        previousBatch = Config.diskCopierCopiesPerCycle;
        Config.diskCopierTicksPerCycle = 100;
        Config.diskCopierCopiesPerCycle = 8;
        ModItems.disk = new ItemDisk();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
    }

    @AfterEach
    void cleanup() {
        ModItems.disk = previous;
        Config.diskCopierTicksPerCycle = previousTicks;
        Config.diskCopierCopiesPerCycle = previousBatch;
        ServerUiTasks.clear();
    }

    private static ItemStack source() {
        ItemStack disk = new ItemStack(ModItems.disk);
        ItemDisk.setProgramString(disk, "name \"copy\" every 20 ticks do print(1) end");
        ProgramDocument.ensure(disk);
        NBTTagCompound labels = new NBTTagCompound();
        NBTTagList positions = new NBTTagList();
        positions.appendTag(new NBTTagIntArray(new int[] { 1, 64, 2 }));
        labels.setTag("chest", positions);
        disk.getTagCompound()
            .setTag("sfm:labels", labels);
        return disk;
    }

    private static ItemStack blank() {
        return new ItemStack(ModItems.disk);
    }

    private static TileEntityDiskCopier machine() throws Exception {
        AuditRegressionTest.FakeWorld world = AuditRegressionTest.world();
        TileEntityDiskCopier copier = new TileEntityDiskCopier();
        copier.setWorldObj(world);
        copier.xCoord = POS.x;
        copier.yCoord = POS.y;
        copier.zCoord = POS.z;
        world.tiles.put(POS, copier);
        copier.setInventorySlotContents(0, source());
        copier.setInventorySlotContents(1, blank());
        return copier;
    }

    private static void ticks(TileEntityDiskCopier machine, int count) {
        for (int i = 0; i < count; i++) machine.updateEntity();
    }

    @Test
    void copyKeepsProgramAndLabelsButUsesFreshIdentityAndNoStateOrForeignMetadata() {
        ItemStack source = source(), medium = blank();
        source.getTagCompound()
            .setTag(ProgramStateStore.NBT_KEY, new NBTTagCompound());
        source.getTagCompound()
            .setString("display", "source-only");
        source.getTagCompound()
            .setString("sfm:last_item_recovery", "source-only");
        NBTTagCompound original = (NBTTagCompound) source.getTagCompound()
            .copy();
        DiskCopyPlan plan = DiskCopyPlan.prepare(source, medium);
        ItemStack copy = plan.complete(source, medium);
        assertEquals(original, source.getTagCompound());
        assertNull(medium.getTagCompound());
        assertNotSame(source, copy);
        assertNotSame(medium, copy);
        assertNotEquals(ProgramDocument.read(source).id, ProgramDocument.read(copy).id);
        assertEquals(0, ProgramDocument.read(copy).revision);
        assertEquals(ItemDisk.getProgramString(source), ItemDisk.getProgramString(copy));
        assertEquals(
            source.getTagCompound()
                .getTag("sfm:labels"),
            copy.getTagCompound()
                .getTag("sfm:labels"));
        assertFalse(
            copy.getTagCompound()
                .hasKey(ProgramStateStore.NBT_KEY));
        assertFalse(
            copy.getTagCompound()
                .hasKey("display"));
        assertFalse(
            copy.getTagCompound()
                .hasKey("sfm:last_item_recovery"));
        copy.getTagCompound()
            .getCompoundTag("sfm:labels")
            .removeTag("chest");
        assertTrue(
            source.getTagCompound()
                .getCompoundTag("sfm:labels")
                .hasKey("chest"));
        assertThrows(IllegalArgumentException.class, () -> plan.complete(source, medium));
    }

    @Test
    void referenceCopyRetainsExactlyItsExecutionBearerAndNeverIntroducesSourceOrProjectOwnership() {
        ItemStack source = source();
        source.getTagCompound()
            .removeTag(ItemDisk.NBT_PROGRAM);
        NBTTagCompound reference = new NBTTagCompound();
        reference.setInteger("format", 1);
        reference.setString("grant", String.join("", java.util.Collections.nCopies(64, "a")));
        reference.setString("digest", String.join("", java.util.Collections.nCopies(64, "b")));
        source.getTagCompound()
            .setTag(ProgramDiskData.NBT_REFERENCE, reference);
        source.getTagCompound()
            .setString("sfm:project", "not-copied");
        ItemStack medium = blank();
        ItemStack copied = DiskCopyPlan.prepare(source, medium)
            .complete(source, medium);
        assertEquals(ProgramDiskData.readReference(source), ProgramDiskData.readReference(copied));
        assertEquals(
            3,
            copied.getTagCompound()
                .func_150296_c()
                .size()); // identity, reference, labels only
        assertFalse(
            copied.getTagCompound()
                .hasKey(ItemDisk.NBT_PROGRAM));
        assertFalse(
            copied.getTagCompound()
                .hasKey("sfm:project"));
        assertNotSame(
            reference,
            copied.getTagCompound()
                .getTag(ProgramDiskData.NBT_REFERENCE));
    }

    @Test
    void refusesRecoveryEvenWrongTypeAndNonblankTargetsWithoutChangingEitherStack() {
        for (String key : Arrays.asList(
            "sfm:recovery_item",
            "sfm:recovery_fluid",
            ProgramDiskData.NBT_REFERENCE,
            ProgramDocument.NBT_KEY,
            "sfm:labels")) {
            ItemStack source = source(), medium = blank();
            source.getTagCompound()
                .setString(key, "invalid");
            NBTTagCompound before = (NBTTagCompound) source.getTagCompound()
                .copy();
            assertThrows(RuntimeException.class, () -> DiskCopyPlan.prepare(source, medium));
            assertEquals(before, source.getTagCompound());
            assertNull(medium.getTagCompound());
        }
        for (ItemStack medium : Arrays.asList(source(), new ItemStack(new Item()), new ItemStack(ModItems.disk, 2))) {
            assertThrows(IllegalArgumentException.class, () -> DiskCopyPlan.prepare(source(), medium));
        }
    }

    @Test
    void rejectsMalformedCoordinateListsDeepNbtAndOversizedDisksBeforeRecursiveCopy() {
        ItemStack malformed = source();
        NBTTagList wrong = new NBTTagList();
        wrong.appendTag(new NBTTagIntArray(new int[] { 1, 2 }));
        malformed.getTagCompound()
            .getCompoundTag("sfm:labels")
            .setTag("chest", wrong);
        assertThrows(IllegalArgumentException.class, () -> DiskCopyPlan.prepare(malformed, blank()));
        ItemStack disk = source();
        NBTTagCompound child = disk.getTagCompound();
        for (int i = 0; i < 100; i++) {
            NBTTagCompound next = new NBTTagCompound();
            child.setTag("child", next);
            child = next;
        }
        ItemStack deep = disk;
        assertThrows(IllegalArgumentException.class, () -> DiskCopyPlan.prepare(deep, blank()));
        ItemStack oversized = source();
        oversized.getTagCompound()
            .setByteArray("huge", new byte[ProgramStateStore.MAX_DISK_NBT_BYTES + 1]);
        assertThrows(IllegalArgumentException.class, () -> DiskCopyPlan.prepare(oversized, blank()));
    }

    @Test
    void exactBindingRejectsReplacedStacksAndInPlaceChangesBeforeCompletion() {
        for (int change = 0; change < 5; change++) {
            ItemStack source = source(), medium = blank();
            DiskCopyPlan plan = DiskCopyPlan.prepare(source, medium);
            ItemStack currentSource = source, currentMedium = medium;
            if (change == 0) currentSource = source.copy();
            if (change == 1) currentMedium = medium.copy();
            if (change == 2) source.stackSize = 2;
            if (change == 3) source.getTagCompound()
                .setString("changed", "during work");
            if (change == 4) medium.setTagCompound(new NBTTagCompound());
            ItemStack s = currentSource, m = currentMedium;
            assertThrows(IllegalArgumentException.class, () -> plan.complete(s, m));
        }
    }

    @Test
    void machinePublishesOnlyAfterOneHundredTicksAndNeverConsumesOrMutatesSource() throws Exception {
        TileEntityDiskCopier copier = machine();
        ItemStack source = copier.getStackInSlot(0), medium = copier.getStackInSlot(1);
        ticks(copier, 99);
        assertEquals(99, copier.getProgress());
        assertSame(source, copier.getStackInSlot(0));
        assertSame(medium, copier.getStackInSlot(1));
        assertNull(copier.getStackInSlot(2));
        ticks(copier, 1);
        assertSame(source, copier.getStackInSlot(0));
        assertNull(copier.getStackInSlot(1));
        ItemStack output = copier.getStackInSlot(2);
        assertNotNull(output);
        assertEquals(1, output.stackSize);
        ticks(copier, 200);
        assertSame(output, copier.getStackInSlot(2));
        for (int i = 1; i < TileEntityDiskCopier.CAPACITY; i++)
            copier.setInventorySlotContents(TileEntityDiskCopier.outputSlot(i), blank());
        copier.setInventorySlotContents(1, blank());
        ticks(copier, 200);
        assertSame(output, copier.getStackInSlot(2));
        assertNotNull(copier.getStackInSlot(1));
        assertSame(output, copier.decrStackSize(2, 1));
        ticks(copier, 100);
        assertNotNull(copier.getStackInSlot(2));
        assertNotSame(output, copier.getStackInSlot(2));
        assertNull(copier.getStackInSlot(1));
    }

    @Test
    void sourceSwapOutputReplacementUnloadAndInPlaceEditCannotCompleteStaleWork() throws Exception {
        for (int change = 0; change < 4; change++) {
            TileEntityDiskCopier copier = machine();
            ticks(copier, 99);
            if (change == 0) copier.setInventorySlotContents(
                0,
                copier.getStackInSlot(0)
                    .copy());
            if (change == 1) copier.setInventorySlotContents(2, blank());
            if (change == 2) copier.onChunkUnload();
            if (change == 3) copier.getStackInSlot(0)
                .getTagCompound()
                .setString("changed", "yes");
            ticks(copier, 1);
            assertNotNull(copier.getStackInSlot(1));
            if (change != 1) assertNull(copier.getStackInSlot(2));
        }
    }

    @Test
    void normalWorkTicksDoNotReparseSourceAndInvalidInputsAreNotRetriedEveryTick() throws Exception {
        TileEntityDiskCopier copier = machine();
        class Counted extends NBTTagCompound {

            int reads;

            @Override
            public net.minecraft.nbt.NBTBase getTag(String key) {
                reads++;
                return super.getTag(key);
            }
        }
        Counted tags = new Counted();
        tags.setString(ItemDisk.NBT_PROGRAM, ItemDisk.getProgramString(copier.getStackInSlot(0)));
        copier.getStackInSlot(0)
            .setTagCompound(tags);
        ticks(copier, 1);
        int preparedReads = tags.reads;
        ticks(copier, 98);
        assertEquals(preparedReads, tags.reads);
        ticks(copier, 1);
        assertTrue(tags.reads > preparedReads);
        copier.getStackInSlotOnClosing(2);
        copier.setInventorySlotContents(1, blank());
        tags.setString("sfm:recovery_item", "blocked");
        ticks(copier, 1);
        preparedReads = tags.reads;
        ticks(copier, 200);
        assertEquals(preparedReads, tags.reads);
        assertNotNull(copier.getStackInSlot(1));
        assertNull(copier.getStackInSlot(2));
    }

    @Test
    void reentrantMetadataReadCannotConsumeReplacementMedia() throws Exception {
        TileEntityDiskCopier copier = machine();
        ItemStack replacement = blank();
        boolean[] replace = { false };
        NBTTagCompound tags = new NBTTagCompound() {

            @Override
            public net.minecraft.nbt.NBTBase getTag(String key) {
                if (replace[0]) {
                    replace[0] = false;
                    copier.setInventorySlotContents(1, replacement);
                }
                return super.getTag(key);
            }
        };
        tags.setString(ItemDisk.NBT_PROGRAM, ItemDisk.getProgramString(copier.getStackInSlot(0)));
        copier.getStackInSlot(0)
            .setTagCompound(tags);
        ticks(copier, 99);
        replace[0] = true;
        ticks(copier, 1);
        assertSame(replacement, copier.getStackInSlot(1));
        assertNull(copier.getStackInSlot(2));
        assertEquals(0, copier.getProgress());
    }

    @Test
    void tileReplacementDuringFinalMetadataValidationCannotPublishIntoAnOrphanedCopier() throws Exception {
        TileEntityDiskCopier copier = machine();
        boolean[] replace = { false };
        NBTTagCompound tags = new NBTTagCompound() {

            @Override
            public net.minecraft.nbt.NBTBase getTag(String key) {
                if (replace[0]) {
                    replace[0] = false;
                    ((AuditRegressionTest.FakeWorld) copier.getWorldObj()).tiles.put(POS, new TileEntityDiskCopier());
                }
                return super.getTag(key);
            }
        };
        tags.setString(ItemDisk.NBT_PROGRAM, ItemDisk.getProgramString(copier.getStackInSlot(0)));
        copier.getStackInSlot(0)
            .setTagCompound(tags);
        ticks(copier, 99);
        replace[0] = true;
        ticks(copier, 1);
        assertNotNull(copier.getStackInSlot(1));
        assertNull(copier.getStackInSlot(2));
        assertEquals(0, copier.getProgress());
    }

    @Test
    void invalidOversizedPhysicalStacksAreNeverTruncatedOrConsumed() throws Exception {
        TileEntityDiskCopier copier = machine();
        ItemStack source = copier.getStackInSlot(0);
        source.stackSize = 4;
        copier.setInventorySlotContents(0, source);
        ticks(copier, 200);
        assertEquals(4, copier.getStackInSlot(0).stackSize);
        assertNotNull(copier.getStackInSlot(1));
        assertNull(copier.getStackInSlot(2));
    }

    @Test
    void batchUsesOneSourceSnapshotAndGivesEveryCopyIndependentIdentityAndLabels() {
        ItemStack source = source();
        class Counted extends NBTTagCompound {

            int copies;

            @Override
            public net.minecraft.nbt.NBTBase copy() {
                copies++;
                return super.copy();
            }
        }
        Counted tags = new Counted();
        tags.setString(ItemDisk.NBT_PROGRAM, ItemDisk.getProgramString(source));
        tags.setTag(
            "sfm:labels",
            source.getTagCompound()
                .getTag("sfm:labels"));
        source.setTagCompound(tags);
        ItemStack[] media = new ItemStack[8];
        for (int i = 0; i < media.length; i++) media[i] = blank();
        DiskCopyPlan plan = DiskCopyPlan.prepareBatch(source, media);
        assertEquals(1, tags.copies, "Source NBT is captured once for the whole batch");
        ItemStack[] copies = plan.completeBatch(source, media);
        java.util.Set<java.util.UUID> identities = new java.util.HashSet<>();
        for (ItemStack disk : copies) {
            assertTrue(identities.add(ProgramDocument.read(disk).id));
            assertEquals(ItemDisk.getProgramString(source), ItemDisk.getProgramString(disk));
            assertFalse(
                disk.getTagCompound()
                    .hasKey(ProgramStateStore.NBT_KEY));
        }
        copies[0].getTagCompound()
            .getCompoundTag("sfm:labels")
            .removeTag("chest");
        for (int i = 1; i < copies.length; i++) assertTrue(
            copies[i].getTagCompound()
                .getCompoundTag("sfm:labels")
                .hasKey("chest"));
        assertTrue(
            source.getTagCompound()
                .getCompoundTag("sfm:labels")
                .hasKey("chest"));
        ItemStack duplicate = blank();
        assertThrows(
            IllegalArgumentException.class,
            () -> DiskCopyPlan.prepareBatch(source, new ItemStack[] { duplicate, duplicate }));
        assertThrows(IllegalArgumentException.class, () -> DiskCopyPlan.prepareBatch(source, new ItemStack[9]));
    }

    @Test
    void eightCopiesPublishTogetherAfterOneSecondAndQueueRemainsUnconsumedBeforeThen() throws Exception {
        Config.diskCopierTicksPerCycle = 20;
        TileEntityDiskCopier copier = machine();
        for (int i = 1; i < 8; i++) copier.setInventorySlotContents(TileEntityDiskCopier.mediumSlot(i), blank());
        ticks(copier, 19);
        for (int i = 0; i < 8; i++) {
            assertNotNull(copier.getStackInSlot(TileEntityDiskCopier.mediumSlot(i)));
            assertNull(copier.getStackInSlot(TileEntityDiskCopier.outputSlot(i)));
        }
        ticks(copier, 1);
        java.util.Set<java.util.UUID> identities = new java.util.HashSet<>();
        for (int i = 0; i < 8; i++) {
            assertNull(copier.getStackInSlot(TileEntityDiskCopier.mediumSlot(i)));
            ItemStack copy = copier.getStackInSlot(TileEntityDiskCopier.outputSlot(i));
            assertNotNull(copy);
            assertEquals(1, copy.stackSize);
            assertTrue(identities.add(ProgramDocument.read(copy).id));
        }
        assertNotNull(copier.getStackInSlot(TileEntityDiskCopier.SOURCE));
        assertEquals(17, copier.getSizeInventory());
    }

    @Test
    void partialOutputSpaceAndConfiguredBatchLimitConsumeOnlyTheirCorrespondingBlanks() throws Exception {
        Config.diskCopierTicksPerCycle = 20;
        TileEntityDiskCopier copier = machine();
        for (int i = 1; i < 8; i++) copier.setInventorySlotContents(TileEntityDiskCopier.mediumSlot(i), blank());
        ItemStack blocker = blank();
        for (int i = 0; i < 6; i++) copier.setInventorySlotContents(TileEntityDiskCopier.outputSlot(i), blocker.copy());
        ticks(copier, 20);
        for (int i = 0; i < 8; i++)
            assertEquals(i >= 2, copier.getStackInSlot(TileEntityDiskCopier.mediumSlot(i)) != null);
        for (int i = 0; i < 8; i++) assertNotNull(copier.getStackInSlot(TileEntityDiskCopier.outputSlot(i)));
        ticks(copier, 100);
        assertNotNull(copier.getStackInSlot(TileEntityDiskCopier.mediumSlot(2)));

        copier = machine();
        Config.diskCopierCopiesPerCycle = 2;
        for (int i = 1; i < 8; i++) copier.setInventorySlotContents(TileEntityDiskCopier.mediumSlot(i), blank());
        for (int run = 1; run <= 4; run++) {
            ticks(copier, 20);
            int filled = 0;
            for (int i = 0; i < 8; i++) if (copier.getStackInSlot(TileEntityDiskCopier.outputSlot(i)) != null) filled++;
            assertEquals(run * 2, filled, "Remaining media must automatically start another cycle");
        }
    }

    @Test
    void cycleDurationIsCapturedAndAnyReplacedBatchMediumPreventsPartialPublication() throws Exception {
        Config.diskCopierTicksPerCycle = 20;
        TileEntityDiskCopier copier = machine();
        for (int i = 1; i < 8; i++) copier.setInventorySlotContents(TileEntityDiskCopier.mediumSlot(i), blank());
        ticks(copier, 1);
        Config.diskCopierTicksPerCycle = 100;
        assertEquals(20, copier.getCycleTicks());
        ticks(copier, 18);
        ItemStack changed = copier.getStackInSlot(TileEntityDiskCopier.mediumSlot(7));
        changed.setTagCompound(new NBTTagCompound()); // Exact null-vs-empty metadata binding changed in place.
        ticks(copier, 1);
        for (int i = 0; i < 8; i++) {
            assertNotNull(copier.getStackInSlot(TileEntityDiskCopier.mediumSlot(i)));
            assertNull(copier.getStackInSlot(TileEntityDiskCopier.outputSlot(i)));
        }
        assertEquals(0, copier.getProgress());
    }

    @Test
    void automationSidesAndClientTicksPreserveMedia() throws Exception {
        TileEntityDiskCopier copier = machine();
        assertArrayEquals(new int[] { 0 }, copier.getAccessibleSlotsFromSide(1));
        int[] outputs = java.util.stream.IntStream.range(0, TileEntityDiskCopier.CAPACITY)
            .map(TileEntityDiskCopier::outputSlot)
            .toArray();
        int[] inputs = java.util.stream.IntStream.range(0, TileEntityDiskCopier.CAPACITY)
            .map(TileEntityDiskCopier::mediumSlot)
            .toArray();
        assertArrayEquals(outputs, copier.getAccessibleSlotsFromSide(0));
        for (int side = 2; side < 6; side++) {
            assertArrayEquals(inputs, copier.getAccessibleSlotsFromSide(side));
            assertTrue(copier.canInsertItem(1, blank(), side));
            assertFalse(copier.canExtractItem(0, source(), side));
        }
        assertTrue(copier.canInsertItem(0, source(), 1));
        assertFalse(copier.canInsertItem(0, blank(), 1));
        assertFalse(copier.canInsertItem(2, blank(), 0));
        assertTrue(copier.canExtractItem(2, source(), 0));
        java.lang.reflect.Field remote = net.minecraft.world.World.class.getDeclaredField("isRemote");
        remote.setAccessible(true);
        remote.setBoolean(copier.getWorldObj(), true);
        ticks(copier, 200);
        assertEquals(0, copier.getProgress());
        assertNotNull(copier.getStackInSlot(1));
        assertNull(copier.getStackInSlot(2));
    }
}
