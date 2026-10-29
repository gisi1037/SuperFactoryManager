package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramDiskBinding;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.program.ProgramDocument;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;

class ProgramDiskDataTest {

    private static final String TOKEN = hex('a'), DIGEST = hex('b');

    @Test
    void legacyDisksAndUnrelatedMetadataKeepTheirSourceAndLazyIdentity() {
        ItemStack disk = new ItemStack(new Item());
        assertEquals(ProgramDiskData.Mode.LEGACY, ProgramDiskData.mode(disk));
        assertEquals("", ItemDisk.getProgramString(disk));
        assertNull(disk.getTagCompound());
        ItemDisk.setProgramString(disk, "name \"legacy\"");
        disk.getTagCompound()
            .setString("other:metadata", "keep");
        ProgramDocument.ensure(disk);
        ProgramDiskBinding binding = ProgramDiskBinding.capture(disk);
        ProgramDocument.advance(disk);
        assertTrue(binding.isCurrent(disk), "Legacy layout/document revisions do not change the executable");
        assertEquals("name \"legacy\"", ItemDisk.getProgramString(disk));
        assertEquals(
            "keep",
            disk.getTagCompound()
                .getString("other:metadata"));
        ItemDisk.setProgramString(disk, "name \"changed\"");
        assertFalse(binding.isCurrent(disk));
    }

    @Test
    void wrongSourceTagCannotBeSilentlyOpenedOrOverwrittenAsEmpty() {
        ItemStack disk = new ItemStack(new Item());
        disk.setTagCompound(new NBTTagCompound());
        disk.getTagCompound()
            .setInteger(ItemDisk.NBT_PROGRAM, 1);
        assertUnreadableAndUnchanged(disk);
    }

    @Test
    void validReferenceContainsOnlyIdentityAndCannotBeEditedAsLegacySource() {
        ItemStack disk = referenceDisk();
        assertEquals(ProgramDiskData.Mode.REFERENCE, ProgramDiskData.mode(disk));
        assertEquals(TOKEN, ProgramDiskData.readReference(disk).token);
        assertEquals(DIGEST, ProgramDiskData.readReference(disk).digest);
        NBTTagCompound before = (NBTTagCompound) disk.getTagCompound()
            .copy();
        assertThrows(IllegalStateException.class, () -> ItemDisk.getProgramString(disk));
        assertThrows(IllegalStateException.class, () -> ItemDisk.setProgramString(disk, ""));
        assertThrows(IllegalStateException.class, () -> ProgramDocument.ensure(disk));
        assertThrows(IllegalStateException.class, () -> ProgramDocument.advance(disk));
        assertEquals(before, disk.getTagCompound());
    }

    @Test
    void malformedMixedAndFutureReferenceFormatsArePreservedWithoutFallback() {
        List<Consumer<ItemStack>> mutations = Arrays.asList(
            disk -> disk.getTagCompound()
                .setString(ItemDisk.NBT_PROGRAM, ""),
            disk -> disk.getTagCompound()
                .setString(ProgramDiskData.NBT_REFERENCE, "invalid"),
            disk -> reference(disk).setInteger("format", 2),
            disk -> reference(disk).setLong("format", 1),
            disk -> reference(disk).removeTag("grant"),
            disk -> reference(disk).setString("grant", hex('A')),
            disk -> reference(disk).setString("grant", "../escape"),
            disk -> reference(disk).setString("digest", DIGEST.substring(1)),
            disk -> reference(disk).setInteger("digest", 2),
            disk -> reference(disk).setBoolean("future", true));
        for (Consumer<ItemStack> mutation : mutations) {
            ItemStack disk = referenceDisk();
            mutation.accept(disk);
            assertUnreadableAndUnchanged(disk);
        }
    }

    @Test
    void referenceBindingInvalidatesOnExactBuildGrantAndDocumentChanges() {
        List<Consumer<ItemStack>> mutations = Arrays.asList(
            disk -> reference(disk).setString("grant", hex('c')),
            disk -> reference(disk).setString("digest", hex('c')),
            disk -> reference(disk).setInteger("format", 2),
            disk -> reference(disk).setBoolean("future", true),
            disk -> disk.getTagCompound()
                .setString(ItemDisk.NBT_PROGRAM, ""),
            disk -> disk.getTagCompound()
                .getCompoundTag(ProgramDocument.NBT_KEY)
                .setLong("revision", 1),
            disk -> disk.getTagCompound()
                .removeTag(ProgramDocument.NBT_KEY));
        for (Consumer<ItemStack> mutation : mutations) {
            ItemStack disk = referenceDisk();
            addDocument(disk);
            ProgramDiskBinding binding = ProgramDiskBinding.capture(disk);
            assertTrue(binding.isCurrent(disk));
            assertFalse(binding.isCurrent(disk.copy()), "An identical physical copy is a distinct activation");
            disk.getTagCompound()
                .setString("labels:test", "unrelated");
            assertTrue(binding.isCurrent(disk));
            mutation.accept(disk);
            assertFalse(binding.isCurrent(disk));
        }
        ItemStack disk = referenceDisk();
        ProgramDiskBinding binding = ProgramDiskBinding.capture(disk);
        addDocument(disk);
        assertFalse(binding.isCurrent(disk));
    }

    @Test
    void referenceStateInvalidatesButStillFlushesCommittedValuesBeforeSchemaReplacement() {
        ItemStack disk = referenceDisk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema());
        state.flush();
        assertTrue(state.isCurrent(disk));
        state.write(0, DataValue.integer(41));
        reference(disk).setString("grant", hex('c'));
        assertFalse(state.isCurrent(disk));
        assertTrue(state.flush(), "Execution identity and ownership of committed heap values are separate fences");
        assertEquals(
            41,
            ProgramStateStore.activate(disk, schema())
                .read(0)
                .asLong());
        assertFalse(state.isCurrent(disk));
    }

    @Test
    void legacyToReferenceSwitchCannotReuseEmptySourceStateBinding() {
        ItemStack disk = new ItemStack(new Item());
        ProgramStateStore state = ProgramStateStore.activate(disk, schema());
        state.flush();
        state.write(0, DataValue.integer(7));
        disk.getTagCompound()
            .setTag(ProgramDiskData.NBT_REFERENCE, reference(referenceDisk()).copy());
        assertFalse(state.isCurrent(disk));
        assertTrue(state.flush());
        assertEquals(
            7,
            ProgramStateStore.activate(disk, schema())
                .read(0)
                .asLong());
    }

    @Test
    void serializedReferenceCopiesRestoreSeparateHeapStateWithoutSourceOrFileAccess() throws Exception {
        ItemStack disk = referenceDisk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema());
        state.write(0, DataValue.integer(17));
        state.flush();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(disk.getTagCompound(), bytes);
        ItemStack copy = new ItemStack(new Item());
        copy.setTagCompound(CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray())));
        assertEquals(ProgramDiskData.readReference(disk), ProgramDiskData.readReference(copy));
        ProgramStateStore restored = ProgramStateStore.activate(copy, schema());
        restored.write(0, DataValue.integer(99));
        assertEquals(
            17,
            state.read(0)
                .asLong());
        assertTrue(state.isCurrent(disk));
        assertFalse(state.isCurrent(copy));
        assertTrue(restored.isCurrent(copy));
        assertFalse(
            copy.getTagCompound()
                .hasKey(ItemDisk.NBT_PROGRAM));
    }

    @Test
    void malformedReferenceCannotClaimStateEvenForAnEmptySchema() {
        ItemStack disk = referenceDisk();
        reference(disk).setInteger("format", 2);
        NBTTagCompound before = (NBTTagCompound) disk.getTagCompound()
            .copy();
        assertThrows(IllegalStateException.class, () -> ProgramStateStore.activate(disk, ProgramStateSchema.EMPTY));
        assertEquals(before, disk.getTagCompound());
    }

    @Test
    void cleanStateFlushRejectsOversizedReferenceDescendantsWithoutChangingSavedState() {
        ItemStack disk = referenceDisk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema());
        state.flush();
        assertFalse(state.isDirty());
        char[] oversized = new char[ProgramStateStore.MAX_DISK_NBT_BYTES + 1];
        Arrays.fill(oversized, 'a');
        reference(disk).setString("digest", new String(oversized));
        NBTTagCompound before = (NBTTagCompound) disk.getTagCompound()
            .copy();
        assertFalse(state.isCurrent(disk));
        assertThrows(IllegalStateException.class, state::flush);
        assertEquals(before, disk.getTagCompound());
        assertFalse(state.isDirty());
    }

    private static void assertUnreadableAndUnchanged(ItemStack disk) {
        NBTTagCompound before = (NBTTagCompound) disk.getTagCompound()
            .copy();
        assertEquals(ProgramDiskData.Mode.INVALID, ProgramDiskData.mode(disk));
        assertThrows(IllegalStateException.class, () -> ProgramDiskData.readReference(disk));
        assertThrows(IllegalStateException.class, () -> ItemDisk.getProgramString(disk));
        assertThrows(IllegalStateException.class, () -> ItemDisk.setProgramString(disk, ""));
        assertThrows(IllegalStateException.class, () -> ProgramDocument.ensure(disk));
        assertEquals(before, disk.getTagCompound());
    }

    private static ProgramStateSchema schema() {
        return new ProgramStateSchema(Arrays.asList(new ProgramStateSchema.Declaration("runs", DataValue.integer(0))));
    }

    private static ItemStack referenceDisk() {
        ItemStack disk = new ItemStack(new Item());
        NBTTagCompound root = new NBTTagCompound(), tag = new NBTTagCompound();
        tag.setInteger("format", ProgramDiskData.REFERENCE_FORMAT);
        tag.setString("grant", TOKEN);
        tag.setString("digest", DIGEST);
        root.setTag(ProgramDiskData.NBT_REFERENCE, tag);
        disk.setTagCompound(root);
        return disk;
    }

    private static NBTTagCompound reference(ItemStack disk) {
        return disk.getTagCompound()
            .getCompoundTag(ProgramDiskData.NBT_REFERENCE);
    }

    private static void addDocument(ItemStack disk) {
        NBTTagCompound document = new NBTTagCompound();
        document.setInteger("format", 1);
        document.setLong("idMost", 1);
        document.setLong("idLeast", 2);
        document.setLong("revision", 0);
        disk.getTagCompound()
            .setTag(ProgramDocument.NBT_KEY, document);
    }

    private static String hex(char c) {
        char[] chars = new char[64];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
