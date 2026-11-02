package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.FluidIdentity;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;

/** Stable recipe identity inputs: no world, registry enumeration, live provider, or recipe selection. */
class IdentityFingerprintTest {

    @Test
    void itemEncodingHasAnIndependentSha256GoldenVector() {
        // SHA-256 of SFM-ID\0, version=1, kind=1, UTF-16BE name with character count,
        // metadata=7, then length=1 and the canonical null-NBT byte. All integers are big-endian.
        ItemIdentity key = ItemIdentity.of("minecraft:stone", 7, null, new QueryBudget());
        assertEquals(
            "b995e68eaa671a72a7120f8e8f1fda9ebd4218935aec5aba47a259dc6a328c99",
            hex(key.fingerprint(new QueryBudget())));
    }

    @Test
    void reorderedCompoundsAndHashCollisionsHaveCanonicalFingerprints() {
        NBTTagCompound first = new NBTTagCompound(), second = new NBTTagCompound();
        first.setInteger("Aa", 12);
        first.setString("BB", "exact");
        second.setString("BB", "exact");
        second.setInteger("Aa", 12);
        assertArrayEquals(item(first), item(second));
        second.setInteger("Aa", 13);
        assertFalse(Arrays.equals(item(first), item(second)));
    }

    @Test
    void nullEmptyNumericTypesAndListOrderStayDistinct() {
        NBTTagCompound integer = new NBTTagCompound(), longer = new NBTTagCompound();
        integer.setInteger("n", 1);
        longer.setLong("n", 1);
        assertFalse(Arrays.equals(item(null), item(new NBTTagCompound())));
        assertFalse(Arrays.equals(item(integer), item(longer)));
        NBTTagList first = new NBTTagList(), second = new NBTTagList();
        first.appendTag(new NBTTagInt(1));
        first.appendTag(new NBTTagInt(2));
        second.appendTag(new NBTTagInt(2));
        second.appendTag(new NBTTagInt(1));
        integer.setTag("list", first);
        longer = (NBTTagCompound) integer.copy();
        longer.setTag("list", second);
        assertFalse(Arrays.equals(item(integer), item(longer)));
    }

    @Test
    void exactRegistryAndMetadataValuesDoNotUseDisplayTextOrWildcardMatching() {
        byte[] original = ItemIdentity.of("test:part", 1, null, new QueryBudget())
            .fingerprint(new QueryBudget());
        for (ItemIdentity different : new ItemIdentity[] { ItemIdentity.of("test:other", 1, null, new QueryBudget()),
            ItemIdentity.of("test:part", 2, null, new QueryBudget()),
            ItemIdentity.of("test:part", 32767, null, new QueryBudget()) })
            assertFalse(Arrays.equals(original, different.fingerprint(new QueryBudget())));
    }

    @Test
    void callerMutationCannotChangeAnIdentityOrAnotherReturnedDigest() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("heat", 12);
        ItemIdentity identity = ItemIdentity.of("test:part", 0, tag, new QueryBudget());
        byte[] original = identity.fingerprint(new QueryBudget()), expected = original.clone();
        Arrays.fill(original, (byte) 0);
        tag.setInteger("heat", 99);
        assertArrayEquals(expected, identity.fingerprint(new QueryBudget()));
    }

    @Test
    void fluidAmountAndTransientJavaObjectIdentityAreNotDurableRecipeIdentity() {
        Fluid fluid = new Fluid("test:fingerprint");
        FluidIdentity first = FluidIdentity.fromStack(TestFluid.of(fluid, 1), new QueryBudget());
        FluidIdentity second = FluidIdentity.fromStack(TestFluid.of(fluid, 1000), new QueryBudget());
        FluidIdentity equivalentRegistryName = FluidIdentity
            .fromStack(TestFluid.of(new Fluid("test:fingerprint"), 1), new QueryBudget());
        assertArrayEquals(first.fingerprint(new QueryBudget()), second.fingerprint(new QueryBudget()));
        assertArrayEquals(first.fingerprint(new QueryBudget()), equivalentRegistryName.fingerprint(new QueryBudget()));
        assertFalse(
            Arrays.equals(
                first.fingerprint(new QueryBudget()),
                ItemIdentity.of("test:fingerprint", 0, null, new QueryBudget())
                    .fingerprint(new QueryBudget())));
    }

    @Test
    void fluidTagsRemainExactAndImmutable() {
        Fluid fluid = new Fluid("fingerprint_fluid");
        FluidStack stack = TestFluid.of(fluid, 1000);
        FluidIdentity noTag = FluidIdentity.fromStack(stack, new QueryBudget());
        stack.tag = new NBTTagCompound();
        FluidIdentity emptyTag = FluidIdentity.fromStack(stack, new QueryBudget());
        stack.tag.setInteger("quality", 5);
        FluidIdentity tagged = FluidIdentity.fromStack(stack, new QueryBudget());
        byte[] before = tagged.fingerprint(new QueryBudget());
        stack.tag.setInteger("quality", 9);
        assertArrayEquals(before, tagged.fingerprint(new QueryBudget()));
        assertFalse(Arrays.equals(noTag.fingerprint(new QueryBudget()), emptyTag.fingerprint(new QueryBudget())));
        assertFalse(Arrays.equals(emptyTag.fingerprint(new QueryBudget()), before));
    }

    @Test
    void boundedHashingUsesNoProviderCallsAndRefusesWorkBeforeAllocating() {
        ItemIdentity identity = ItemIdentity.of("test:item", 0, null, new QueryBudget());
        QueryBudget budget = new QueryBudget();
        assertEquals(32, identity.fingerprint(budget).length);
        assertTrue(budget.getSlotWork() > 0);
        assertTrue(budget.getAllocations() > 0);
        assertEquals(0, budget.getProviderCalls());
        assertEquals(0, budget.getPositions());
        budget.chargeSlots(QueryBudget.MAX_SLOT_WORK - budget.getSlotWork());
        int allocations = budget.getAllocations();
        assertThrows(IllegalArgumentException.class, () -> identity.fingerprint(budget));
        assertEquals(allocations, budget.getAllocations());
        assertThrows(IllegalArgumentException.class, () -> identity.fingerprint(null));
    }

    private static byte[] item(NBTTagCompound tag) {
        return ItemIdentity.of("test:item", 0, tag, new QueryBudget())
            .fingerprint(new QueryBudget());
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) result.append(String.format(java.util.Locale.ROOT, "%02x", value & 255));
        return result.toString();
    }
}
