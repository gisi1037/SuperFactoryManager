package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlItemNbtInspectionTest {

    private static final String ID = "gregtech:gt.360k_Helium_Coolantcell";

    @Test
    void coolantHeatAndTagAreExactSnapshotReadsNotMetadataOrLiveValues() {
        NBTTagCompound tags = new NBTTagCompound();
        tags.setInteger("heat", 55872);
        tags.setByte("tag", (byte) 1);
        ItemIdentity key = ItemIdentity.of(ID, 15, tags, new QueryBudget());
        int hash = key.hashCode();
        tags.setInteger("heat", 999);
        assertEquals(55872, eval(key, "nbtinteger(\"/heat\")").asLong());
        assertEquals(1, eval(key, "nbtinteger(\"/tag\")").asLong());
        assertEquals("int", eval(key, "nbttype(\"/heat\")").asString());
        assertEquals("byte", eval(key, "nbttype(\"/tag\")").asString());
        assertEquals(15, key.getMetadata());
        assertEquals(hash, key.hashCode());
        assertEquals(999, tags.getInteger("heat"));
        DataValue keys = eval(key, "nbtkeys()");
        assertEquals(DataType.dynamicArray(DataType.STRING), keys.getType());
        assertEquals(
            "heat",
            keys.get(0)
                .asString());
        assertEquals(
            "tag",
            keys.get(1)
                .asString());
        assertEquals(2, eval(key, "nbtlength()").asLong());
    }

    @Test
    void integerWidthsSignedLimitsAndTextScalarsKeepTheirTypes() {
        NBTTagCompound tags = new NBTTagCompound();
        tags.setByte("b", (byte) -128);
        tags.setShort("s", (short) -32768);
        tags.setInteger("i", Integer.MIN_VALUE);
        tags.setLong("l", Long.MIN_VALUE);
        tags.setFloat("f", 1.25f);
        tags.setDouble("d", Double.POSITIVE_INFINITY);
        tags.setString("text", "hello \u03bb");
        ItemIdentity key = ItemIdentity.of(ID, 15, tags, new QueryBudget());
        assertEquals(-128, eval(key, "nbtinteger(\"/b\")").asLong());
        assertEquals(-32768, eval(key, "nbtinteger(\"/s\")").asLong());
        assertEquals(Integer.MIN_VALUE, eval(key, "nbtinteger(\"/i\")").asLong());
        assertEquals(Long.MIN_VALUE, eval(key, "nbtinteger(\"/l\")").asLong());
        assertEquals("1.25", eval(key, "nbttext(\"/f\")").asString());
        assertEquals("Infinity", eval(key, "nbttext(\"/d\")").asString());
        assertEquals("hello \u03bb", eval(key, "nbtstring(\"/text\")").asString());
        assertThrows(IllegalArgumentException.class, () -> eval(key, "nbtinteger(\"/f\")"));
        assertThrows(IllegalArgumentException.class, () -> eval(key, "nbtstring(\"/i\")"));
        assertThrows(IllegalArgumentException.class, () -> eval(key, "nbttext()"));
    }

    @Test
    void nestedCompoundsListsArraysAndEscapedLiteralKeysAreAddressable() {
        NBTTagCompound root = new NBTTagCompound(), nested = new NBTTagCompound();
        nested.setLong("a/b~c", 42);
        nested.setInteger("", 7);
        root.setTag("GT.Item", nested);
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagString("first"));
        list.appendTag(new NBTTagString("second"));
        root.setTag("names", list);
        root.setByteArray("bytes", new byte[] { 1, -2 });
        root.setIntArray("ints", new int[] { 8, -9 });
        ItemIdentity key = ItemIdentity.of(ID, 15, root, new QueryBudget());
        assertEquals(42, eval(key, "nbtinteger(\"/GT.Item/a~1b~0c\")").asLong());
        assertEquals(7, eval(key, "nbtinteger(\"/GT.Item/\")").asLong());
        assertEquals("second", eval(key, "nbtstring(\"/names/1\")").asString());
        assertEquals(-2, eval(key, "nbtinteger(\"/bytes/1\")").asLong());
        assertEquals(-9, eval(key, "nbtinteger(\"/ints/1\")").asLong());
        assertEquals(2, eval(key, "nbtlength(\"/names\")").asLong());
        assertEquals("int_array", eval(key, "nbttype(\"/ints\")").asString());
        assertFalse(eval(key, "hasnbt(\"/names/01\")").asBoolean());
        assertFalse(eval(key, "hasnbt(\"/names/2\")").asBoolean());
        assertFalse(eval(key, "hasnbt(\"/ints/0/x\")").asBoolean());
        assertThrows(IllegalArgumentException.class, () -> eval(key, "hasnbt(\"/bad~2\")"));
        assertThrows(IllegalArgumentException.class, () -> eval(key, "hasnbt(\"heat\")"));
    }

    @Test
    void missingRootAndEmptyCompoundAreDifferentAndNoGetterInventsZero() {
        ItemIdentity missing = ItemIdentity.of(ID, 15, null, new QueryBudget());
        ItemIdentity empty = ItemIdentity.of(ID, 15, new NBTTagCompound(), new QueryBudget());
        assertFalse(eval(missing, "hasnbt()").asBoolean());
        assertTrue(eval(empty, "hasnbt()").asBoolean());
        assertEquals("missing", eval(missing, "nbttype()").asString());
        assertEquals("compound", eval(empty, "nbttype()").asString());
        assertFalse(eval(empty, "hasnbt(\"/heat\")").asBoolean());
        assertThrows(IllegalArgumentException.class, () -> eval(empty, "nbtinteger(\"/heat\")"));
        assertEquals(0, eval(empty, "nbtkeys()").size());
    }

    @Test
    void skipUnselectedContainersAndScalarTypesWithoutCorruptingOffsets() {
        NBTTagCompound tags = new NBTTagCompound();
        tags.setByteArray("a", new byte[] { 1, 2, 3 });
        tags.setIntArray("b", new int[] { 4, 5 });
        tags.setDouble("c", -2.5);
        NBTTagList list = new NBTTagList();
        NBTTagCompound child = new NBTTagCompound();
        child.setString("skip", "value");
        list.appendTag(child);
        tags.setTag("d", list);
        tags.setLong("z", 123);
        ItemIdentity key = ItemIdentity.of(ID, 15, tags, new QueryBudget());
        assertEquals(123, eval(key, "nbtinteger(\"/z\")").asLong());
        assertEquals("value", eval(key, "nbtstring(\"/d/0/skip\")").asString());
        assertEquals(5, eval(key, "nbtkeys()").size());
    }

    @Test
    void boundedKeysAndSharedWorkBudgetRefuseWithoutChangingInput() {
        NBTTagCompound tags = new NBTTagCompound();
        for (int i = 0; i < 257; i++) tags.setInteger("k" + i, i);
        // Construct the legal wide fixture with enough snapshot credit; the read keeps base limits.
        ItemIdentity key = ItemIdentity.of(ID, 15, tags, new QueryBudget(4));
        assertThrows(IllegalArgumentException.class, () -> eval(key, "nbtkeys()"));
        QueryBudget owner = new QueryBudget();
        owner.chargeSlots(QueryBudget.MAX_SLOT_WORK);
        DataRuntime runtime = new DataRuntime(null, 0, null, owner);
        assertThrows(IllegalArgumentException.class, () -> runtime.inspectItemNbt(key, "HASNBT", "/k0"));
        assertEquals(
            257,
            tags.func_150296_c()
                .size());
        assertEquals(256, tags.getInteger("k256"));
    }

    @Test
    void compilerRequiresExactKeysAndAcceptsRealHeatReaderProgram() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new DataCompiler().compile("item(\"" + ID + "\").nbtinteger(\"/heat\")", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.item(ID + ":15"));
        assertNotNull(ResourceSelector.item(ID));
        assertTrue(
            new ProgramBuilder(
                "name \"Heat test\" every 20 ticks { let reactor = label(\"reactor\") "
                    + "for stack in reactor.contents(item(\""
                    + ID
                    + "\")) { if(stack.metadata == 15 and stack.item.hasnbt(\"/heat\")) { "
                    + "print(stack.item.nbtinteger(\"/heat\"), stack.item.nbttype(\"/heat\")) } } }").build()
                        .getErrors()
                        .isEmpty());
    }

    private static DataValue eval(ItemIdentity key, String call) {
        DataCompiler compiler = new DataCompiler();
        compiler.pushScope();
        DataCompiler.Binding binding = compiler.declareLocal("key", DataType.ITEM_KEY);
        DataRuntime runtime = new DataRuntime(null, compiler.getMaxLocals());
        binding.write(runtime, DataValue.itemIdentity(key));
        return compiler.compile("key." + call, 1, 0)
            .evaluate(runtime);
    }
}
