package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;

class ItemIdentityLiveComparisonTest {

    private static final Item MATCHED = new Item(), OTHER = new Item();
    private static final String ID = "sfm_live_identity:item";
    static {
        try {
            Field field = RegistrySimple.class.getDeclaredField("registryObjects");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) field.get(Item.itemRegistry);
            names.put(ID, MATCHED);
            names.put("sfm_live_identity:other", OTHER);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void everyNormalKindMatchesInScalarsAndListsWithoutRepeatingSnapshotAllocations() {
        NBTBase[] kinds = { new NBTTagByte((byte) -1), new NBTTagShort((short) -2), new NBTTagInt(-3),
            new NBTTagLong(Long.MIN_VALUE), new NBTTagFloat(1.25F), new NBTTagDouble(-2.5D),
            new NBTTagByteArray(new byte[] { -1, 2 }), new NBTTagString("\u0000\ud800\udfff"), list(new NBTTagInt(4)),
            wrapped(new NBTTagInt(5)), new NBTTagIntArray(new int[] { -1, 2 }) };
        NBTTagCompound compound = new NBTTagCompound();
        for (int i = 0; i < kinds.length; i++) {
            compound.setTag("s" + i, kinds[i]);
            compound.setTag("l" + i, list(kinds[i].copy()));
        }
        compound.setTag("empty", new NBTTagList());
        ItemIdentity key = key(compound);
        ItemStack stack = stack((NBTTagCompound) compound.copy());
        QueryBudget budget = new QueryBudget();
        for (int i = 0; i < 8; i++) {
            int before = budget.getSlotWork();
            assertTrue(key.matchesStack(stack, budget));
            assertTrue(budget.getSlotWork() - before <= key.getComparisonWork());
        }
        assertEquals(8, budget.getAllocations(), "Only one comparison control object per walk; no byte/key arrays");
        assertEquals(0, budget.getProviderCalls(), "Native caller owns provider callback accounting");
        assertEquals(0, budget.getPositions());
        assertEquals(key, ItemIdentity.fromStack(stack, new QueryBudget()));
    }

    @Test
    void snapshotKeySidecarsRemainIndependentAndIgnoreCompoundInsertionOrder() {
        NBTTagCompound original = new NBTTagCompound();
        original.setInteger("Aa", 1);
        original.setInteger("BB", 2);
        original.setTag("nested", list(wrapped(new NBTTagInt(3)), wrapped(new NBTTagInt(4))));
        ItemIdentity key = key(original);
        NBTTagCompound reversed = new NBTTagCompound();
        reversed.setTag("nested", list(wrapped(new NBTTagInt(3)), wrapped(new NBTTagInt(4))));
        reversed.setInteger("BB", 2);
        reversed.setInteger("Aa", 1);
        original.removeTag("Aa");
        original.setInteger("renamed", 1);
        assertFalse(key.matchesStack(stack(original), new QueryBudget()));
        assertTrue(key.matchesStack(stack(reversed), new QueryBudget()));
        reversed.getTagList("nested", 10)
            .getCompoundTagAt(1)
            .setInteger("value", 9);
        assertFalse(key.matchesStack(stack(reversed), new QueryBudget()));
        assertFalse(key.matchesStack(stack((NBTTagCompound) original.copy()), new QueryBudget()));
    }

    @Test
    void metadataRegistryPresenceTypesOrdersAndRawFloatBitsAreExact() {
        ItemIdentity absent = key(null), empty = key(new NBTTagCompound());
        assertTrue(absent.matchesStack(stack(null), new QueryBudget()));
        assertFalse(absent.matchesStack(stack(new NBTTagCompound()), new QueryBudget()));
        assertFalse(empty.matchesStack(stack(null), new QueryBudget()));
        assertTrue(empty.matchesStack(stack(new NBTTagCompound()), new QueryBudget()));
        ItemStack wrongMetadata = stack(null);
        wrongMetadata.setItemDamage(8);
        assertFalse(absent.matchesStack(wrongMetadata, new QueryBudget()));
        assertFalse(absent.matchesStack(new ItemStack(OTHER, 1, 7), new QueryBudget()));
        assertFalse(absent.matchesStack(null, new QueryBudget()));
        assertFalse(
            key(wrapped(new NBTTagByte((byte) 1))).matchesStack(stack(wrapped(new NBTTagInt(1))), new QueryBudget()));
        assertFalse(
            key(wrapped(new NBTTagFloat(0F))).matchesStack(stack(wrapped(new NBTTagFloat(-0F))), new QueryBudget()));
        assertFalse(
            key(wrapped(new NBTTagDouble(0D))).matchesStack(stack(wrapped(new NBTTagDouble(-0D))), new QueryBudget()));
        assertFalse(
            key(wrapped(new NBTTagFloat(Float.intBitsToFloat(0x7fc00001))))
                .matchesStack(stack(wrapped(new NBTTagFloat(Float.intBitsToFloat(0x7fc00002)))), new QueryBudget()));
        assertFalse(
            key(wrapped(new NBTTagDouble(Double.longBitsToDouble(0x7ff8000000000001L)))).matchesStack(
                stack(wrapped(new NBTTagDouble(Double.longBitsToDouble(0x7ff8000000000002L)))),
                new QueryBudget()));
        assertFalse(
            key(wrapped(list(new NBTTagInt(1), new NBTTagInt(2))))
                .matchesStack(stack(wrapped(list(new NBTTagInt(2), new NBTTagInt(1)))), new QueryBudget()));
        assertFalse(
            key(wrapped(new NBTTagByteArray(new byte[] { 1, 2 })))
                .matchesStack(stack(wrapped(new NBTTagByteArray(new byte[] { 2, 1 }))), new QueryBudget()));
        assertFalse(
            key(wrapped(new NBTTagIntArray(new int[] { 1, 2 })))
                .matchesStack(stack(wrapped(new NBTTagIntArray(new int[] { 2, 1 }))), new QueryBudget()));
        NBTTagList typedEmpty = list(new NBTTagInt(1));
        typedEmpty.removeTag(0);
        assertFalse(key(wrapped(new NBTTagList())).matchesStack(stack(wrapped(typedEmpty)), new QueryBudget()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void missingExpectedKeyIsMismatchButPresentNullAndEncounteredCorruptionFault() throws Exception {
        ItemIdentity expected = key(wrapped(new NBTTagInt(1)));
        NBTTagCompound renamed = new NBTTagCompound();
        renamed.setInteger("different", 1);
        assertFalse(expected.matchesStack(stack(renamed), new QueryBudget()));
        assertThrows(
            IllegalArgumentException.class,
            () -> expected.matchesStack(stack(wrapped(null)), new QueryBudget()));
        assertThrows(
            IllegalArgumentException.class,
            () -> expected.matchesStack(stack(wrapped(new NBTTagEnd())), new QueryBudget()));
        assertThrows(
            IllegalArgumentException.class,
            () -> expected.matchesStack(stack(new NBTTagCompound() {}), new QueryBudget()));
        NBTTagList valid = list(new NBTTagInt(1), new NBTTagInt(2));
        ItemIdentity listKey = key(wrapped(valid));
        NBTTagList corrupt = list(new NBTTagInt(1), new NBTTagInt(2));
        Field elements = NBTTagList.class.getDeclaredField("tagList");
        elements.setAccessible(true);
        ((List<NBTBase>) elements.get(corrupt)).set(1, new NBTTagByte((byte) 2));
        assertThrows(
            IllegalArgumentException.class,
            () -> listKey.matchesStack(stack(wrapped(corrupt)), new QueryBudget()));
    }

    @Test
    void successfulWorkBoundAndAllocationFailuresAreConservativeAndAtomic() {
        NBTTagCompound complex = new NBTTagCompound();
        complex.setTag("nested", list(wrapped(new NBTTagString("value"))));
        complex.setIntArray("ints", new int[] { 1, 2, 3 });
        ItemIdentity key = key(complex);
        QueryBudget work = new QueryBudget();
        work.chargeSlots(QueryBudget.MAX_SLOT_WORK - key.getComparisonWork());
        assertTrue(key.matchesStack(stack(complex), work));
        assertTrue(work.getSlotWork() <= QueryBudget.MAX_SLOT_WORK);
        QueryBudget noWork = new QueryBudget();
        noWork.chargeSlots(QueryBudget.MAX_SLOT_WORK);
        assertThrows(IllegalArgumentException.class, () -> key.matchesStack(stack(complex), noWork));
        assertEquals(QueryBudget.MAX_SLOT_WORK, noWork.getSlotWork());
        assertEquals(0, noWork.getAllocations());
        QueryBudget noAllocations = new QueryBudget();
        noAllocations.chargeAllocation(QueryBudget.MAX_ALLOCATIONS);
        assertThrows(IllegalArgumentException.class, () -> key.matchesStack(stack(complex), noAllocations));
        assertEquals(QueryBudget.MAX_ALLOCATIONS, noAllocations.getAllocations());
        assertThrows(IllegalArgumentException.class, () -> key.matchesStack(stack(complex), null));
        assertEquals(key, key((NBTTagCompound) complex.copy()));
    }

    @Test
    void mismatchedHeadersDoNotTraverseLargeOrInvalidUnvisitedDescendants() {
        ItemIdentity expected = key(wrapped(new NBTTagByteArray(new byte[] { 1 })));
        QueryBudget budget = new QueryBudget();
        assertFalse(expected.matchesStack(stack(wrapped(new NBTTagByteArray(new byte[4096]))), budget));
        assertTrue(budget.getSlotWork() < 100, "Different array length stops before payload traversal");
        assertThrows(
            IllegalArgumentException.class,
            () -> expected.matchesStack(stack(wrapped(new NBTTagByteArray(new byte[8193]))), new QueryBudget()),
            "An encountered oversized array faults before its differing header can return false");
        NBTTagCompound deep = new NBTTagCompound();
        for (int i = 0; i < 64; i++) deep = wrapped(deep);
        assertFalse(
            key(new NBTTagCompound()).matchesStack(stack(deep), new QueryBudget()),
            "Early mismatch denies authority without claiming validation of unvisited deep data");
        NBTTagCompound expectedDeep = new NBTTagCompound();
        for (int i = 1; i < 16; i++) expectedDeep = wrapped(expectedDeep);
        assertTrue(key(expectedDeep).matchesStack(stack(expectedDeep), new QueryBudget()));
        assertFalse(key(expectedDeep).matchesStack(stack(wrapped(expectedDeep)), new QueryBudget()));
    }

    @Test
    void largerLiveCompoundAndListHeadersRejectWithinTheExpectedKeyWorkBound() {
        NBTTagCompound many = new NBTTagCompound();
        NBTTagList manyElements = new NBTTagList();
        for (int i = 0; i < 100; i++) {
            many.setInteger("key" + i, i);
            manyElements.appendTag(new NBTTagInt(i));
        }
        ItemIdentity emptyCompound = key(new NBTTagCompound());
        ItemIdentity oneElement = key(wrapped(list(new NBTTagInt(1))));
        QueryBudget compoundBudget = new QueryBudget(), listBudget = new QueryBudget();
        compoundBudget.chargeSlots(QueryBudget.MAX_SLOT_WORK - emptyCompound.getComparisonWork());
        listBudget.chargeSlots(QueryBudget.MAX_SLOT_WORK - oneElement.getComparisonWork());
        assertFalse(emptyCompound.matchesStack(stack(many), compoundBudget));
        assertFalse(oneElement.matchesStack(stack(wrapped(manyElements)), listBudget));
        assertEquals(1, compoundBudget.getAllocations());
        assertEquals(1, listBudget.getAllocations());
    }

    private static ItemIdentity key(NBTTagCompound nbt) {
        return ItemIdentity.of(ID, 7, nbt, new QueryBudget());
    }

    private static ItemStack stack(NBTTagCompound nbt) {
        ItemStack stack = new ItemStack(MATCHED, 1, 7);
        stack.setTagCompound(nbt);
        return stack;
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
}
