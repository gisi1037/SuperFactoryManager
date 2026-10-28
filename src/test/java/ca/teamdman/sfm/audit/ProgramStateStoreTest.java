package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.ProgramStateSchema.Declaration;

class ProgramStateStoreTest {

    @Test
    void valuesStayInHeapUntilBoundaryFlushAndReloadActualSerializedNbt() throws Exception {
        ItemStack disk = disk();
        ProgramStateSchema schema = schema(
            declaration("runs", DataValue.integer(0)),
            declaration("config", config(8, true)),
            declaration("steps", array(1, 2, 4)));
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        assertTrue(state.isDirty());
        assertFalse(
            disk.getTagCompound()
                .hasKey(ProgramStateStore.NBT_KEY));
        state.write(0, DataValue.integer(17));
        state.write(1, config(32, false));
        assertEquals(
            17,
            state.read(0)
                .asLong());
        assertFalse(
            disk.getTagCompound()
                .hasKey(ProgramStateStore.NBT_KEY));
        assertTrue(state.flush());
        assertFalse(state.isDirty());
        NBTBase published = disk.getTagCompound()
            .getTag(ProgramStateStore.NBT_KEY);
        assertFalse(state.flush());
        assertSame(
            published,
            disk.getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(disk.getTagCompound(), bytes);
        ItemStack reloaded = new ItemStack(new Item());
        reloaded.setTagCompound(CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray())));
        ProgramStateStore restored = ProgramStateStore.activate(reloaded, schema);
        assertFalse(restored.isDirty());
        assertEquals(
            17,
            restored.read(0)
                .asLong());
        assertEquals(
            32,
            restored.read(1)
                .get("target")
                .asLong());
        assertFalse(
            restored.read(1)
                .get("enabled")
                .asBoolean());
        assertEquals(
            4,
            restored.read(2)
                .get(2)
                .asLong());
        assertMetadata(reloaded);
    }

    @Test
    void compatibleEditsReorderSlotsAndChangeDefaultsWithoutRewritingOrResettingValues() {
        ItemStack disk = disk();
        ProgramStateStore first = ProgramStateStore
            .activate(disk, schema(declaration("runs", DataValue.integer(0)), declaration("config", config(8, true))));
        first.write(0, DataValue.integer(9));
        first.flush();
        NBTBase published = disk.getTagCompound()
            .getTag(ProgramStateStore.NBT_KEY);
        ItemDisk.setProgramString(disk, "-- formatting edit\nSTATE $runs = 100");
        ProgramStateStore edited = ProgramStateStore.activate(
            disk,
            schema(declaration("config", config(99, false)), declaration("runs", DataValue.integer(100))));
        assertEquals(
            9,
            edited.read(1)
                .asLong());
        assertEquals(
            8,
            edited.read(0)
                .get("target")
                .asLong());
        assertTrue(
            edited.read(0)
                .get("enabled")
                .asBoolean());
        assertFalse(edited.isDirty());
        assertFalse(edited.flush());
        assertSame(
            published,
            disk.getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY));
    }

    @Test
    void sameCountRenameDropsOldAndInitializesNewOnlyAfterAcceptedPublication() {
        ItemStack disk = disk();
        ProgramStateStore first = ProgramStateStore.activate(
            disk,
            schema(declaration("old", DataValue.integer(1)), declaration("keep", DataValue.integer(2))));
        first.write(0, DataValue.integer(88));
        first.write(1, DataValue.integer(99));
        first.flush();
        NBTBase original = disk.getTagCompound()
            .getTag(ProgramStateStore.NBT_KEY);
        ProgramStateStore renamed = ProgramStateStore.activate(
            disk,
            schema(declaration("newName", DataValue.integer(7)), declaration("keep", DataValue.integer(0))));
        assertEquals(
            7,
            renamed.read(0)
                .asLong());
        assertEquals(
            99,
            renamed.read(1)
                .asLong());
        assertTrue(renamed.isDirty());
        assertFalse(
            renamed.getActivationWarnings()
                .isEmpty());
        assertSame(
            original,
            disk.getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY));
        renamed.flush();
        assertNotSame(
            original,
            disk.getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY));
        ProgramStateStore restoredName = ProgramStateStore.activate(
            disk,
            schema(declaration("old", DataValue.integer(3)), declaration("keep", DataValue.integer(0))));
        assertEquals(
            3,
            restoredName.read(0)
                .asLong(),
            "Removed entries must not resurrect");
        assertMetadata(disk);
    }

    @Test
    void incompatibleActivationAndAssignmentPreserveThePriorEnvelopeAndCommittedValue() {
        ItemStack disk = disk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema(declaration("value", array(1, 2))));
        state.flush();
        NBTTagCompound original = (NBTTagCompound) disk.getTagCompound()
            .copy();
        IllegalStateException typeChange = assertThrows(
            IllegalStateException.class,
            () -> ProgramStateStore.activate(disk, schema(declaration("value", DataValue.bool(false)))));
        assertTrue(
            typeChange.getMessage()
                .contains("$value"));
        assertTrue(
            typeChange.getMessage()
                .contains("Reset program variables"));
        assertThrows(
            IllegalStateException.class,
            () -> ProgramStateStore.activate(disk, schema(declaration("value", array(1, 2, 3)))));
        assertThrows(IllegalStateException.class, () -> state.write(0, DataValue.integer(4)));
        assertEquals(
            2,
            state.read(0)
                .get(1)
                .asLong());
        assertFalse(state.isDirty());
        assertEquals(original, disk.getTagCompound());
    }

    @Test
    void corruptStateIsRejectedWholeAndOnlyExplicitResetCanReplaceIt() {
        List<Consumer<NBTTagCompound>> corruptions = new ArrayList<>();
        corruptions.add(envelope -> envelope.setInteger("version", 99));
        corruptions.add(envelope -> envelope.setLong("revision", -1));
        corruptions.add(envelope -> envelope.setInteger("count", 2));
        corruptions.add(envelope -> envelope.setString("unknown", "field"));
        corruptions.add(envelope -> entry(envelope).setString("type", "unrecognized type"));
        corruptions.add(envelope -> entry(envelope).removeTag("value"));
        corruptions.add(
            envelope -> entry(envelope).getCompoundTag("value")
                .setInteger("value", 1));
        corruptions.add(envelope -> {
            NBTTagCompound value = entry(envelope).getCompoundTag("value");
            value.setByte("kind", (byte) 2);
            value.setByte("value", (byte) 2);
        });
        corruptions.add(envelope -> {
            NBTTagList entries = envelope.getTagList("entries", 10);
            entries.appendTag(
                entries.getCompoundTagAt(0)
                    .copy());
            envelope.setInteger("count", 2);
        });
        corruptions.add(envelope -> {
            NBTTagList wrongKind = new NBTTagList();
            wrongKind.appendTag(new NBTTagString("wrong"));
            envelope.setTag("entries", wrongKind);
        });
        for (Consumer<NBTTagCompound> corrupt : corruptions) {
            ItemStack disk = savedScalarDisk();
            corrupt.accept(envelope(disk));
            NBTTagCompound raw = (NBTTagCompound) disk.getTagCompound()
                .copy();
            assertThrows(
                IllegalStateException.class,
                () -> ProgramStateStore.activate(disk, schema(declaration("runs", DataValue.integer(0)))));
            assertEquals(raw, disk.getTagCompound());
            ProgramStateStore reset = ProgramStateStore.reset(disk, schema(declaration("runs", DataValue.integer(5))));
            assertEquals(raw, disk.getTagCompound(), "Preparing reset must preserve corrupt raw NBT");
            assertTrue(reset.flush());
            assertEquals(
                5,
                ProgramStateStore.activate(disk, schema(declaration("runs", DataValue.integer(0))))
                    .read(0)
                    .asLong());
            assertMetadata(disk);
        }
        ItemStack wrongEnvelope = disk();
        wrongEnvelope.getTagCompound()
            .setString(ProgramStateStore.NBT_KEY, "damaged");
        assertThrows(
            IllegalStateException.class,
            () -> ProgramStateStore.activate(wrongEnvelope, ProgramStateSchema.EMPTY));
        assertTrue(
            ProgramStateStore.reset(wrongEnvelope, ProgramStateSchema.EMPTY)
                .flush());
        assertEquals(0, envelope(wrongEnvelope).getInteger("count"));
    }

    @Test
    void sameScalarsAndSameImmutableRootDoNotDirtyButNewCollectionsNeedNoDeepEquality() {
        ItemStack disk = disk();
        ProgramStateStore state = ProgramStateStore.activate(
            disk,
            schema(
                declaration("number", DataValue.integer(4)),
                declaration("flag", DataValue.bool(true)),
                declaration("array", array(1, 2))));
        state.flush();
        state.write(0, DataValue.integer(4));
        state.write(1, DataValue.bool(true));
        state.write(2, state.read(2));
        assertFalse(state.isDirty());
        assertFalse(state.flush());
        state.write(2, array(1, 2));
        assertTrue(state.isDirty(), "Distinct immutable roots can dirty without recursively comparing collections");
        assertTrue(state.flush());
    }

    @Test
    void disksAndCopiesOwnIndependentLiveValuesAndPublicationTrees() {
        ItemStack disk = savedScalarDisk();
        ItemStack copy = disk.copy();
        ProgramStateSchema schema = schema(declaration("runs", DataValue.integer(0)));
        ProgramStateStore first = ProgramStateStore.activate(disk, schema);
        ProgramStateStore second = ProgramStateStore.activate(copy, schema);
        first.write(0, DataValue.integer(100));
        assertEquals(
            6,
            second.read(0)
                .asLong());
        assertEquals(
            6,
            entry(envelope(disk)).getCompoundTag("value")
                .getLong("value"));
        first.flush();
        assertEquals(
            6,
            ProgramStateStore.activate(copy, schema)
                .read(0)
                .asLong());
        second.write(0, DataValue.integer(200));
        second.flush();
        assertEquals(
            100,
            first.read(0)
                .asLong());
    }

    @Test
    void externalReplacementOrRevisionAndSourceChangesInvalidateTheOwner() {
        ProgramStateSchema schema = schema(declaration("runs", DataValue.integer(0)));
        ItemStack disk = savedScalarDisk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        assertTrue(state.isCurrent(disk));
        assertFalse(state.isCurrent(disk.copy()));
        disk.getTagCompound()
            .setTag(ProgramStateStore.NBT_KEY, envelope(disk).copy());
        assertFalse(state.isCurrent(disk));
        state.write(0, DataValue.integer(99));
        NBTBase replacement = disk.getTagCompound()
            .getTag(ProgramStateStore.NBT_KEY);
        ProgramStateStore stale = state;
        assertThrows(IllegalStateException.class, stale::flush);
        assertTrue(state.isDirty());
        assertSame(
            replacement,
            disk.getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY));
        state = ProgramStateStore.activate(disk, schema);
        envelope(disk).setLong("revision", envelope(disk).getLong("revision") + 1);
        assertFalse(state.isCurrent(disk));
        state.write(0, DataValue.integer(98));
        ProgramStateStore revisionStale = state;
        assertThrows(IllegalStateException.class, revisionStale::flush);
        state = ProgramStateStore.activate(disk, schema);
        ItemDisk.setProgramString(disk, "changed source");
        assertFalse(state.isCurrent(disk));
        state = ProgramStateStore.activate(disk, schema);
        disk.setTagCompound(
            (NBTTagCompound) disk.getTagCompound()
                .copy());
        assertFalse(state.isCurrent(disk));
        state.write(0, DataValue.integer(97));
        ProgramStateStore rootStale = state;
        assertThrows(IllegalStateException.class, rootStale::flush);
    }

    @Test
    void rejectsOversizedWideAndDeepPayloadsWithoutChangingRawNbt() {
        ItemStack disk = savedScalarDisk();
        char[] chars = new char[60000];
        Arrays.fill(chars, 'x');
        for (int i = 0; i < 5; i++) envelope(disk).setString("padding" + i, new String(chars));
        assertRejectedUnchanged(disk);
        disk = savedScalarDisk();
        NBTTagCompound value = entry(envelope(disk)).getCompoundTag("value");
        NBTTagList wide = new NBTTagList();
        for (int i = 0; i < 257; i++) wide.appendTag(integerTag(i));
        value.setByte("kind", (byte) 3);
        value.setTag("value", wide);
        assertRejectedUnchanged(disk);
        disk = savedScalarDisk();
        value = integerTag(0);
        for (int i = 0; i < 33; i++) {
            NBTTagCompound parent = new NBTTagCompound();
            parent.setByte("kind", (byte) 3);
            NBTTagList child = new NBTTagList();
            child.appendTag(value);
            parent.setTag("value", child);
            value = parent;
        }
        entry(envelope(disk)).setTag("value", value);
        assertRejectedUnchanged(disk);
    }

    @Test
    void emptyLegacySchemaNeedsNoNbtPublicationAndRemovedAllStatePublishesEmptySchema() {
        ItemStack legacy = disk();
        ProgramStateStore empty = ProgramStateStore.activate(legacy, ProgramStateSchema.EMPTY);
        assertFalse(empty.isDirty());
        assertFalse(empty.flush());
        assertFalse(
            legacy.getTagCompound()
                .hasKey(ProgramStateStore.NBT_KEY));
        ItemStack disk = savedScalarDisk();
        ProgramStateStore removed = ProgramStateStore.activate(disk, ProgramStateSchema.EMPTY);
        assertTrue(removed.isDirty());
        assertTrue(removed.flush());
        assertEquals(0, envelope(disk).getInteger("count"));
        assertFalse(
            ProgramStateStore.activate(disk, ProgramStateSchema.EMPTY)
                .isDirty());
    }

    @Test
    void laterFailedAssignmentKeepsEarlierHeapCommitForTheBoundaryFlush() {
        ItemStack disk = savedScalarDisk();
        ProgramStateSchema schema = schema(declaration("runs", DataValue.integer(0)));
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        state.write(0, DataValue.integer(41));
        assertThrows(IllegalStateException.class, () -> state.write(0, DataValue.bool(true)));
        assertEquals(
            41,
            state.read(0)
                .asLong());
        assertEquals(
            6,
            entry(envelope(disk)).getCompoundTag("value")
                .getLong("value"));
        state.flush();
        assertEquals(
            41,
            ProgramStateStore.activate(disk, schema)
                .read(0)
                .asLong());
    }

    @Test
    void logicalCellCeilingStillHonorsTransportBudgetAndOversizedDecodedAggregateFailsWhole() {
        List<Declaration> declarations = new ArrayList<>();
        for (int i = 0; i < 15; i++) declarations.add(declaration("array" + i, array(new long[256])));
        declarations.add(declaration("tail", array(new long[240])));
        ProgramStateSchema maximum = new ProgramStateSchema(declarations);
        ItemStack disk = disk();
        NBTTagCompound before = (NBTTagCompound) disk.getTagCompound()
            .copy();
        IllegalStateException transport = assertThrows(
            IllegalStateException.class,
            () -> ProgramStateStore.activate(disk, maximum));
        assertTrue(
            transport.getMessage()
                .contains("transport budget"));
        assertEquals(
            before,
            disk.getTagCompound(),
            "Logical cell limits do not bypass the complete disk transport bound");
        ItemStack oversized = disk();
        ProgramStateStore sample = ProgramStateStore
            .activate(oversized, schema(declaration("array", array(new long[256]))));
        sample.flush();
        NBTTagCompound encoded = entry(envelope(oversized));
        NBTTagList entries = new NBTTagList();
        for (int i = 0; i < 17; i++) {
            NBTTagCompound copy = (NBTTagCompound) encoded.copy();
            copy.setString("name", "array" + i);
            entries.appendTag(copy);
        }
        envelope(oversized).setInteger("count", 17);
        envelope(oversized).setTag("entries", entries);
        assertRejectedUnchanged(oversized);
    }

    @Test
    void sourceOnlyEditInvalidatesBindingButFlushPreservesPriorCommitsBeforeReconciliation() {
        ItemStack disk = savedScalarDisk();
        ProgramStateSchema schema = schema(declaration("runs", DataValue.integer(0)));
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        state.write(0, DataValue.integer(42));
        ItemDisk.setProgramString(disk, "STATE $runs = 100 -- externally changed source");
        assertFalse(state.isCurrent(disk));
        assertTrue(state.flush());
        assertFalse(state.isCurrent(disk), "Flushing old values must not reactivate the old execution binding");
        assertEquals("STATE $runs = 100 -- externally changed source", ItemDisk.getProgramString(disk));
        ProgramStateStore compatible = ProgramStateStore
            .activate(disk, schema(declaration("runs", DataValue.integer(100))));
        assertEquals(
            42,
            compatible.read(0)
                .asLong());
        assertTrue(compatible.isCurrent(disk));
        assertFalse(compatible.isDirty());
        compatible.write(0, DataValue.integer(43));
        ItemDisk.setProgramString(disk, "STATE $runs = TRUE -- incompatible external edit");
        assertFalse(compatible.isCurrent(disk));
        assertTrue(compatible.flush());
        NBTTagCompound preserved = (NBTTagCompound) disk.getTagCompound()
            .copy();
        assertThrows(
            IllegalStateException.class,
            () -> ProgramStateStore.activate(disk, schema(declaration("runs", DataValue.bool(true)))));
        assertEquals(preserved, disk.getTagCompound());
        assertEquals(
            43,
            entry(envelope(disk)).getCompoundTag("value")
                .getLong("value"));
    }

    @Test
    void terminalRevisionCanReloadButNeverPublishesAnUnsupportedNextRevision() {
        ItemStack disk = savedScalarDisk();
        ProgramStateSchema schema = schema(declaration("runs", DataValue.integer(0)));
        envelope(disk).setLong("revision", Long.MAX_VALUE - 2);
        ProgramStateStore beforeTerminal = ProgramStateStore.activate(disk, schema);
        beforeTerminal.write(0, DataValue.integer(7));
        assertTrue(beforeTerminal.flush());
        assertEquals(Long.MAX_VALUE - 1, envelope(disk).getLong("revision"));
        ProgramStateStore terminal = ProgramStateStore.activate(disk, schema);
        assertEquals(
            7,
            terminal.read(0)
                .asLong());
        assertFalse(terminal.flush());
        NBTTagCompound snapshot = (NBTTagCompound) disk.getTagCompound()
            .copy();
        terminal.write(0, DataValue.integer(8));
        IllegalStateException exhausted = assertThrows(IllegalStateException.class, terminal::flush);
        assertTrue(
            exhausted.getMessage()
                .contains("revision is exhausted"));
        assertTrue(terminal.isDirty());
        assertEquals(
            8,
            terminal.read(0)
                .asLong());
        assertEquals(snapshot, disk.getTagCompound());
        assertEquals(
            7,
            ProgramStateStore.activate(disk, schema)
                .read(0)
                .asLong());
        ProgramStateStore reset = ProgramStateStore.reset(disk, schema);
        assertTrue(reset.flush());
        assertEquals(1, envelope(disk).getLong("revision"));
        assertEquals(
            0,
            ProgramStateStore.activate(disk, schema)
                .read(0)
                .asLong());
    }

    @Test
    void stringWeightsMatchJavaModifiedUtfAndAggregateThroughNestedContainers() throws Exception {
        String text = "\u0000\u007f\u0080\u07ff\u0800\ud83d\ude03";
        DataValue value = DataValue.string(text);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new DataOutputStream(bytes).writeUTF(text);
        assertEquals(7, value.getStringCharacterCount());
        assertEquals(16, value.getStringNbtBytes());
        assertEquals(bytes.size() - 2, value.getStringNbtBytes());
        DataValue nested = stringConfig(text, text);
        assertEquals(14, nested.getStringCharacterCount());
        assertEquals(32, nested.getStringNbtBytes());
        assertEquals(
            0,
            DataValue.integer(1)
                .getStringNbtBytes());
        assertEquals(
            0,
            DataValue.bool(true)
                .getStringCharacterCount());
    }

    @Test
    void nestedStringsRoundTripAndCompatibleDefaultsMayHaveDifferentLengths() throws Exception {
        ItemStack disk = disk();
        ProgramStateSchema schema = schema(
            declaration("text", DataValue.string("")),
            declaration("config", stringConfig("a", "longer")));
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        DataValue original = state.read(1);
        DataValue changedNames = original.get("names")
            .with(1, DataValue.string("\u0000\ud83d\ude03"));
        DataValue changed = original.with(
            original.getType()
                .fieldIndex("names"),
            changedNames);
        state.write(0, DataValue.string("saved text"));
        state.write(1, changed);
        assertEquals(
            "longer",
            original.get("names")
                .get(1)
                .asString());
        assertTrue(state.flush());
        assertEquals(1, envelope(disk).getInteger("version"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(disk.getTagCompound(), bytes);
        ItemStack reloaded = new ItemStack(new Item());
        reloaded.setTagCompound(CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray())));
        ProgramStateSchema editedSchema = schema(
            declaration("config", stringConfig("different length", "")),
            declaration("text", DataValue.string("new default")));
        ProgramStateStore restored = ProgramStateStore.activate(reloaded, editedSchema);
        assertFalse(restored.isDirty());
        assertEquals(
            "saved text",
            restored.read(1)
                .asString());
        assertEquals(
            "\u0000\ud83d\ude03",
            restored.read(0)
                .get("names")
                .get(1)
                .asString());
        assertMetadata(reloaded);
        ItemDisk.setProgramString(reloaded, "STATE $text = \"edited default\"");
        restored.write(1, DataValue.string("commit before edit reconciliation"));
        assertFalse(restored.isCurrent(reloaded));
        assertTrue(restored.flush());
        assertEquals(
            "commit before edit reconciliation",
            ProgramStateStore.activate(reloaded, editedSchema)
                .read(1)
                .asString());
    }

    @Test
    void stringGrowthChecksExactCompleteDiskBudgetBeforePublishingHeapOrNbt() throws Exception {
        ItemStack disk = disk();
        ProgramStateSchema schema = schema(declaration("text", DataValue.string("x")));
        ProgramStateStore initial = ProgramStateStore.activate(disk, schema);
        initial.flush();
        fillDiskToBytes(disk, ProgramStateStore.MAX_DISK_NBT_BYTES - 1);
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        NBTBase published = envelope(disk);
        state.write(0, DataValue.string("\u0000")); // Modified UTF uses two bytes for NUL.
        DataValue accepted = state.read(0);
        assertThrows(IllegalStateException.class, () -> state.write(0, DataValue.string("\ud83d\ude03")));
        assertSame(accepted, state.read(0));
        assertSame(published, envelope(disk));
        assertEquals(
            "x",
            entry(envelope(disk)).getCompoundTag("value")
                .getString("value"));
        assertTrue(state.flush());
        assertEquals(ProgramStateStore.MAX_DISK_NBT_BYTES, diskNbtBytes(disk));
        assertEquals(
            "\u0000",
            ProgramStateStore.activate(disk, schema)
                .read(0)
                .asString());
        state.write(0, DataValue.string(""));
        state.write(0, DataValue.string("ab")); // Shrinking releases the exact cached payload capacity.
        assertTrue(state.flush());
        assertEquals(ProgramStateStore.MAX_DISK_NBT_BYTES, diskNbtBytes(disk));
    }

    @Test
    void nestedStringGrowthAndColdMetadataEditsRespectPendingHeapBudget() throws Exception {
        ItemStack disk = disk();
        ProgramStateSchema schema = schema(declaration("config", stringConfig("", "")));
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        state.flush();
        fillDiskToBytes(disk, ProgramStateStore.MAX_DISK_NBT_BYTES - 2);
        DataValue original = state.read(0);
        state.write(0, stringConfig("ab", ""));
        DataValue accepted = state.read(0);
        NBTBase published = envelope(disk);
        assertThrows(IllegalStateException.class, () -> state.write(0, stringConfig("ab", "x")));
        assertSame(accepted, state.read(0));
        assertSame(published, envelope(disk));
        assertEquals(
            "",
            original.get("names")
                .get(0)
                .asString());
        assertTrue(state.flush());
        assertEquals(ProgramStateStore.MAX_DISK_NBT_BYTES, diskNbtBytes(disk));
        String padding = disk.getTagCompound()
            .getString("padding");
        disk.getTagCompound()
            .setString("padding", padding + "x");
        assertThrows(IllegalStateException.class, () -> state.write(0, stringConfig("ab", "y")));
        assertSame(accepted, state.read(0));
        disk.getTagCompound()
            .setString("padding", padding.substring(1));
        state.write(0, stringConfig("abc", ""));
        assertTrue(state.flush());
        assertEquals(ProgramStateStore.MAX_DISK_NBT_BYTES, diskNbtBytes(disk));
    }

    @Test
    void repeatedStringWritesNeitherSerializeMetadataNorReplacePublishedEnvelope() {
        ItemStack disk = disk();
        CountingString probe = new CountingString();
        disk.getTagCompound()
            .setTag("serialization_probe", probe);
        ProgramStateStore state = ProgramStateStore.activate(disk, schema(declaration("text", DataValue.string(""))));
        state.flush();
        NBTBase published = envelope(disk);
        int serializedTagReads = probe.idReads;
        for (int i = 0; i < 1000; i++) state.write(0, DataValue.string(i % 2 == 0 ? "a" : "\u0000\ud83d\ude03"));
        assertEquals(serializedTagReads, probe.idReads, "Heap writes must not serialize the complete disk");
        assertSame(published, envelope(disk));
        assertTrue(state.flush());
        assertTrue(probe.idReads > serializedTagReads);
        state.write(0, state.read(0));
        assertFalse(state.isDirty(), "Reusing the same immutable string is a no-op");
        assertFalse(state.flush());
    }

    @Test
    void malformedSavedStringsPreserveRawStateUntilExplicitReset() {
        List<Consumer<NBTTagCompound>> corruptions = Arrays.asList(
            envelope -> entry(envelope).getCompoundTag("value")
                .setString("value", "\ud800"),
            envelope -> entry(envelope).getCompoundTag("value")
                .setString("value", "\udc00"),
            envelope -> entry(envelope).getCompoundTag("value")
                .setString("value", repeat('x', 4097)),
            envelope -> entry(envelope).getCompoundTag("value")
                .setLong("value", 1),
            envelope -> entry(envelope).getCompoundTag("value")
                .setByte("kind", (byte) 99),
            envelope -> entry(envelope).setString("type", "INTEGER"));
        ProgramStateSchema schema = schema(declaration("text", DataValue.string("default")));
        for (Consumer<NBTTagCompound> corruption : corruptions) {
            ItemStack disk = disk();
            ProgramStateStore.activate(disk, schema)
                .flush();
            corruption.accept(envelope(disk));
            NBTTagCompound raw = (NBTTagCompound) disk.getTagCompound()
                .copy();
            assertThrows(IllegalStateException.class, () -> ProgramStateStore.activate(disk, schema));
            assertEquals(raw, disk.getTagCompound());
            ProgramStateStore reset = ProgramStateStore.reset(disk, schema);
            assertEquals(raw, disk.getTagCompound());
            assertTrue(reset.flush());
            assertEquals(
                "default",
                ProgramStateStore.activate(disk, schema)
                    .read(0)
                    .asString());
        }
    }

    @Test
    void aggregateStateStringCharactersAreBoundedAtDefaultsDecodeAndAssignment() {
        ProgramStateSchema schema = schema(
            declaration("a", DataValue.string(repeat('x', 4096))),
            declaration("b", DataValue.string(repeat('x', 4096))),
            declaration("c", DataValue.string(repeat('x', 4096))),
            declaration("d", DataValue.string(repeat('x', 4095))),
            declaration("tail", DataValue.string("x")));
        ItemStack disk = disk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        state.flush();
        DataValue accepted = state.read(4);
        assertThrows(IllegalStateException.class, () -> state.write(4, DataValue.string("xx")));
        assertSame(accepted, state.read(4));
        assertFalse(state.isDirty());
        assertThrows(
            IllegalArgumentException.class,
            () -> schema(
                declaration("a", DataValue.string(repeat('x', 4096))),
                declaration("b", DataValue.string(repeat('x', 4096))),
                declaration("c", DataValue.string(repeat('x', 4096))),
                declaration("d", DataValue.string(repeat('x', 4096))),
                declaration("tail", DataValue.string("x"))));
        envelope(disk).getTagList("entries", 10)
            .getCompoundTagAt(4)
            .getCompoundTag("value")
            .setString("value", "xx");
        NBTTagCompound raw = (NBTTagCompound) disk.getTagCompound()
            .copy();
        assertThrows(IllegalStateException.class, () -> ProgramStateStore.activate(disk, schema));
        assertEquals(raw, disk.getTagCompound());
    }

    @Test
    void cleanFlushChecksTheActualExternallyReplacedEnvelopeTransportSize() {
        ItemStack disk = disk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema(declaration("text", DataValue.string(""))));
        state.flush();
        NBTTagCompound replacement = (NBTTagCompound) envelope(disk).copy();
        replacement.setString("foreign_padding", repeat('x', ProgramStateStore.MAX_DISK_NBT_BYTES));
        disk.getTagCompound()
            .setTag(ProgramStateStore.NBT_KEY, replacement);
        NBTTagCompound raw = (NBTTagCompound) disk.getTagCompound()
            .copy();
        assertThrows(IllegalStateException.class, state::flush);
        assertEquals(raw, disk.getTagCompound());
    }

    private static DataValue stringConfig(String first, String second) {
        Map<String, DataValue> fields = new LinkedHashMap<>();
        fields.put("names", DataValue.array(Arrays.asList(DataValue.string(first), DataValue.string(second))));
        fields.put("enabled", DataValue.bool(true));
        return DataValue.struct(fields);
    }

    private static String repeat(char value, int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, value);
        return new String(chars);
    }

    private static int diskNbtBytes(ItemStack disk) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.write(disk.getTagCompound(), new DataOutputStream(bytes));
        return bytes.size();
    }

    private static void fillDiskToBytes(ItemStack disk, int target) throws Exception {
        disk.getTagCompound()
            .setString("padding", "");
        int length = target - diskNbtBytes(disk);
        assertTrue(length >= 0);
        disk.getTagCompound()
            .setString("padding", repeat('p', length));
        assertEquals(target, diskNbtBytes(disk));
    }

    private static final class CountingString extends NBTTagString {

        private int idReads;

        private CountingString() {
            super("serialization probe");
        }

        @Override
        public byte getId() {
            idReads++;
            return super.getId();
        }
    }

    private static void assertRejectedUnchanged(ItemStack disk) {
        NBTTagCompound raw = (NBTTagCompound) disk.getTagCompound()
            .copy();
        assertThrows(
            IllegalStateException.class,
            () -> ProgramStateStore.activate(disk, schema(declaration("runs", DataValue.integer(0)))));
        assertEquals(raw, disk.getTagCompound());
    }

    private static ItemStack savedScalarDisk() {
        ItemStack disk = disk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema(declaration("runs", DataValue.integer(0))));
        state.write(0, DataValue.integer(6));
        state.flush();
        return disk;
    }

    private static ItemStack disk() {
        ItemStack disk = new ItemStack(new Item());
        NBTTagCompound root = new NBTTagCompound();
        root.setString(ItemDisk.NBT_PROGRAM, "-- test source");
        root.setString("sfm:labels", "labels preserved");
        root.setString("sfm:recovery", "recovery preserved");
        root.setString("sfm:graph", "graph preserved");
        disk.setTagCompound(root);
        return disk;
    }

    private static void assertMetadata(ItemStack disk) {
        assertEquals(
            "labels preserved",
            disk.getTagCompound()
                .getString("sfm:labels"));
        assertEquals(
            "recovery preserved",
            disk.getTagCompound()
                .getString("sfm:recovery"));
        assertEquals(
            "graph preserved",
            disk.getTagCompound()
                .getString("sfm:graph"));
        assertEquals("-- test source", ItemDisk.getProgramString(disk));
    }

    private static NBTTagCompound envelope(ItemStack disk) {
        return disk.getTagCompound()
            .getCompoundTag(ProgramStateStore.NBT_KEY);
    }

    private static NBTTagCompound entry(NBTTagCompound envelope) {
        return envelope.getTagList("entries", 10)
            .getCompoundTagAt(0);
    }

    private static NBTTagCompound integerTag(long value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("kind", (byte) 1);
        tag.setLong("value", value);
        return tag;
    }

    private static Declaration declaration(String name, DataValue defaultValue) {
        return new Declaration(name, defaultValue);
    }

    private static ProgramStateSchema schema(Declaration... declarations) {
        return new ProgramStateSchema(Arrays.asList(declarations));
    }

    private static DataValue array(long... elements) {
        List<DataValue> values = new ArrayList<>();
        for (long element : elements) values.add(DataValue.integer(element));
        return DataValue.array(values);
    }

    private static DataValue config(long target, boolean enabled) {
        Map<String, DataValue> fields = new LinkedHashMap<>();
        fields.put("target", DataValue.integer(target));
        fields.put("enabled", DataValue.bool(enabled));
        return DataValue.struct(fields);
    }
}
