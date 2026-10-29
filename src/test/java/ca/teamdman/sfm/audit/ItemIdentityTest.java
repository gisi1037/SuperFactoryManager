package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;

/** Pure identity foundation tests: normal NBT initializes headlessly, without Item/Forge registry bootstrap. */
class ItemIdentityTest {

    private static ItemIdentity key(NBTTagCompound nbt) {
        return ItemIdentity.of("identity_test:item", 7, nbt, new QueryBudget());
    }

    private static NBTTagCompound wrapped(NBTBase tag) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("value", tag);
        return compound;
    }

    private static NBTTagList list(NBTBase... tags) {
        NBTTagList list = new NBTTagList();
        for (NBTBase tag : tags) list.appendTag(tag);
        return list;
    }

    @Test
    void registryAndMetadataAreExactAndEqualityHasTheUsualValueContract() {
        ItemIdentity first = key(null), equal = key(null);
        assertEquals(first, first);
        assertEquals(first, equal);
        assertEquals(equal, first);
        assertEquals(first.hashCode(), equal.hashCode());
        assertNotEquals(first, null);
        assertNotEquals(first, "identity_test:item");
        assertNotEquals(first, ItemIdentity.of("identity_test:other", 7, null, new QueryBudget()));
        assertNotEquals(first, ItemIdentity.of("identity_test:item", 8, null, new QueryBudget()));
        assertNotEquals(ItemIdentity.of("identity_test:item", 32767, null, new QueryBudget()), first);
        assertEquals("identity_test:item", first.getRegistryId());
        assertEquals(7, first.getMetadata());
        assertThrows(IllegalArgumentException.class, () -> ItemIdentity.of("*", 0, null, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> ItemIdentity.of(null, 0, null, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> ItemIdentity.of("test:item", 0, null, null));
    }

    @Test
    void compoundKeyOrderIsCanonicalButNamesAndStringContentsAreExact() {
        NBTTagCompound first = new NBTTagCompound(), second = new NBTTagCompound();
        // Aa and BB collide in String.hashCode: insertion order changes HashMap traversal here.
        first.setInteger("Aa", 1);
        first.setString("BB", "\u0000\ud800\udfff");
        second.setString("BB", "\u0000\ud800\udfff");
        second.setInteger("Aa", 1);
        assertEquals(key(first), key(second));
        assertEquals(key(first).hashCode(), key(second).hashCode());
        second.setString("BB", "\u0000\ud800\udffe");
        assertNotEquals(key(first), key(second));
        second.removeTag("BB");
        second.setString("BC", "\u0000\ud800\udfff");
        assertNotEquals(key(first), key(second));
    }

    @Test
    void inputMutationCannotChangeAKeyOrItsHashSetMembership() {
        byte[] bytes = { 1, 2 };
        int[] ints = { 3, 4 };
        NBTTagCompound nested = wrapped(new NBTTagString("before"));
        NBTTagList list = list(nested);
        NBTTagCompound input = new NBTTagCompound();
        input.setByteArray("bytes", bytes);
        input.setIntArray("ints", ints);
        input.setTag("list", list);
        ItemIdentity snapshot = key(input);
        int originalHash = snapshot.hashCode();
        Set<ItemIdentity> set = new HashSet<>();
        set.add(snapshot);

        NBTTagCompound original = new NBTTagCompound();
        original.setByteArray("bytes", new byte[] { 1, 2 });
        original.setIntArray("ints", new int[] { 3, 4 });
        original.setTag("list", list(wrapped(new NBTTagString("before"))));
        bytes[0] = 9;
        ints[0] = 9;
        nested.setString("value", "after");
        list.appendTag(new NBTTagCompound());
        input.setInteger("added", 1);
        assertEquals(key(original), snapshot);
        assertEquals(originalHash, snapshot.hashCode());
        assertTrue(set.contains(key(original)));
        assertNotEquals(snapshot, key(input));
    }

    @Test
    void everyNormalTagKindWorksAsACompoundValueAndAsAListElement() {
        NBTBase[] tags = { new NBTTagByte((byte) -1), new NBTTagShort((short) -2), new NBTTagInt(-3),
            new NBTTagLong(Long.MIN_VALUE), new NBTTagFloat(1.25F), new NBTTagDouble(-2.5D),
            new NBTTagByteArray(new byte[] { -1, 2 }), new NBTTagString("text"), list(new NBTTagInt(4)),
            wrapped(new NBTTagInt(5)), new NBTTagIntArray(new int[] { -1, 2 }) };
        for (NBTBase tag : tags) {
            assertEquals(key(wrapped(tag)), key(wrapped(tag.copy())));
            assertEquals(key(wrapped(list(tag))), key(wrapped(list(tag.copy()))));
        }
    }

    @Test
    void numericTagTypeAndFloatingPointBitsRemainDistinct() {
        NBTBase[] one = { new NBTTagByte((byte) 1), new NBTTagShort((short) 1), new NBTTagInt(1), new NBTTagLong(1),
            new NBTTagFloat(1), new NBTTagDouble(1) };
        Set<ItemIdentity> keys = new HashSet<>();
        for (NBTBase tag : one) keys.add(key(wrapped(tag)));
        assertEquals(one.length, keys.size());
        assertNotEquals(key(wrapped(new NBTTagFloat(0F))), key(wrapped(new NBTTagFloat(-0F))));
        assertNotEquals(key(wrapped(new NBTTagDouble(0D))), key(wrapped(new NBTTagDouble(-0D))));
        assertNotEquals(
            key(wrapped(new NBTTagFloat(Float.intBitsToFloat(0x7fc00001)))),
            key(wrapped(new NBTTagFloat(Float.intBitsToFloat(0x7fc00002)))));
        assertNotEquals(
            key(wrapped(new NBTTagDouble(Double.longBitsToDouble(0x7ff8000000000001L)))),
            key(wrapped(new NBTTagDouble(Double.longBitsToDouble(0x7ff8000000000002L)))));
    }

    @Test
    void listAndArrayOrderAndEmptyListElementTypeArePreserved() {
        assertNotEquals(
            key(wrapped(list(new NBTTagInt(1), new NBTTagInt(2)))),
            key(wrapped(list(new NBTTagInt(2), new NBTTagInt(1)))));
        assertNotEquals(
            key(wrapped(new NBTTagByteArray(new byte[] { 1, 2 }))),
            key(wrapped(new NBTTagByteArray(new byte[] { 2, 1 }))));
        assertNotEquals(
            key(wrapped(new NBTTagIntArray(new int[] { 1, 2 }))),
            key(wrapped(new NBTTagIntArray(new int[] { 2, 1 }))));
        assertNotEquals(
            key(wrapped(new NBTTagByteArray(new byte[] { 1 }))),
            key(wrapped(new NBTTagIntArray(new int[] { 1 }))));
        NBTTagList typedEmpty = list(new NBTTagInt(1));
        typedEmpty.removeTag(0);
        assertNotEquals(key(wrapped(new NBTTagList())), key(wrapped(typedEmpty)));
        assertNotEquals(key(null), key(new NBTTagCompound()));
        assertEquals(key(new NBTTagCompound()), key(new NBTTagCompound()));
    }

    @Test
    void oversizedDeepNodeHeavyAndCyclicInputsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> key(wrapped(new NBTTagByteArray(new byte[8192]))));
        assertThrows(IllegalArgumentException.class, () -> key(wrapped(new NBTTagIntArray(new int[2048]))));
        assertThrows(IllegalArgumentException.class, () -> key(wrapped(new NBTTagString(new String(new char[5000])))));
        NBTTagCompound deep = new NBTTagCompound();
        for (int i = 1; i < 16; i++) deep = wrapped(deep);
        assertNotNull(key(deep));
        NBTTagCompound tooDeep = wrapped(deep);
        assertThrows(IllegalArgumentException.class, () -> key(tooDeep));
        NBTTagCompound many = new NBTTagCompound();
        for (int i = 0; i < 1024; i++) many.setByte("n" + i, (byte) 0);
        assertThrows(IllegalArgumentException.class, () -> key(many));
        NBTTagCompound cycle = new NBTTagCompound();
        cycle.setTag("self", cycle);
        assertThrows(IllegalArgumentException.class, () -> key(cycle));
    }

    @Test
    void sharedBudgetChargesCopiesAndWorkAndRejectsBeforeExceedingItsLimits() {
        QueryBudget budget = new QueryBudget();
        ItemIdentity.of("test:item", 0, wrapped(new NBTTagByteArray(new byte[128])), budget);
        assertTrue(budget.getAllocations() >= 128);
        assertTrue(budget.getSlotWork() >= 128);
        assertEquals(0, budget.getProviderCalls());
        assertEquals(0, budget.getPositions());
        QueryBudget noAllocation = new QueryBudget();
        noAllocation.chargeAllocation(QueryBudget.MAX_ALLOCATIONS);
        assertThrows(IllegalArgumentException.class, () -> ItemIdentity.of("test:item", 0, null, noAllocation));
        assertEquals(QueryBudget.MAX_ALLOCATIONS, noAllocation.getAllocations());
        QueryBudget noWork = new QueryBudget();
        noWork.chargeSlots(QueryBudget.MAX_SLOT_WORK);
        assertThrows(IllegalArgumentException.class, () -> ItemIdentity.of("test:item", 0, null, noWork));
        assertEquals(QueryBudget.MAX_SLOT_WORK, noWork.getSlotWork());
        assertEquals(0, noWork.getAllocations());
    }

    @Test
    @SuppressWarnings("unchecked")
    void corruptEndNullHeterogeneousAndSubclassTagsAreRejected() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> key(wrapped(new NBTTagEnd())));
        assertThrows(IllegalArgumentException.class, () -> key(wrapped(null)));
        assertThrows(IllegalArgumentException.class, () -> key(new NBTTagCompound() {}));
        assertThrows(IllegalArgumentException.class, () -> key(wrapped(new NBTTagByte((byte) 1) {})));
        Field elements = NBTTagList.class.getDeclaredField("tagList");
        elements.setAccessible(true);
        NBTTagList heterogeneous = list(new NBTTagInt(1));
        ((List<NBTBase>) elements.get(heterogeneous)).add(new NBTTagByte((byte) 2));
        assertThrows(IllegalArgumentException.class, () -> key(wrapped(heterogeneous)));
        NBTTagList endList = new NBTTagList();
        ((List<NBTBase>) elements.get(endList)).add(new NBTTagEnd());
        assertThrows(IllegalArgumentException.class, () -> key(wrapped(endList)));
    }
}
