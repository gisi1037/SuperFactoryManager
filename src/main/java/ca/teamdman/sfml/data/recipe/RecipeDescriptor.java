package ca.teamdman.sfml.data.recipe;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ca.teamdman.sfml.data.FluidIdentity;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;

/** Immutable inspection data. Native matching policies describe requirements; they grant no crafting authority. */
public final class RecipeDescriptor {

    public static final int MAX_SLOTS = 64, MAX_ALTERNATIVES = 64, MAX_REQUIREMENTS = 32;

    public enum Matching {

        EXACT(1),
        GT_NATIVE_ITEM(2),
        ORE_OR_EXPLICIT(3),
        GT_NATIVE_FLUID(4);

        private final int encoding;

        Matching(int encoding) {
            this.encoding = encoding;
        }
    }

    public static final class ItemPart {

        public final int slot, chance;
        public final ItemIdentity item;
        public final long amount;
        public final Matching matching;
        public final boolean nbtSensitive;
        public final String ore;
        public final List<ItemIdentity> alternatives, displayAlternatives;

        public ItemPart(int slot, ItemIdentity item, long amount, int chance, Matching matching, boolean nbtSensitive,
            String ore, List<ItemIdentity> alternatives, List<ItemIdentity> displayAlternatives) {
            checkPart(slot, amount, chance);
            this.slot = slot;
            this.item = Objects.requireNonNull(item, "Item identity");
            this.amount = amount;
            this.chance = chance;
            this.matching = Objects.requireNonNull(matching, "Item matching policy");
            if (matching == Matching.GT_NATIVE_FLUID) throw invalid("Fluid matching policy on item");
            this.nbtSensitive = nbtSensitive;
            this.ore = text(ore, true);
            if (matching == Matching.ORE_OR_EXPLICIT && ore.isEmpty()) throw invalid("Ore name required");
            if (matching != Matching.ORE_OR_EXPLICIT && !ore.isEmpty()) throw invalid("Unexpected ore predicate");
            this.alternatives = copy(alternatives, MAX_ALTERNATIVES);
            this.displayAlternatives = copy(displayAlternatives, MAX_ALTERNATIVES);
            if (matching != Matching.ORE_OR_EXPLICIT && !alternatives.isEmpty())
                throw invalid("Execution alternatives require an ore-or-explicit predicate");
        }

        public boolean isCatalyst() {
            return amount == 0;
        }
    }

    public static final class FluidAlternative {

        public final FluidIdentity fluid;
        public final long amount;

        public FluidAlternative(FluidIdentity fluid, long amount) {
            this.fluid = Objects.requireNonNull(fluid, "Fluid identity");
            if (amount < 0) throw invalid("Negative fluid quantity");
            this.amount = amount;
        }
    }

    public static final class FluidPart {

        public final int slot, chance;
        public final FluidIdentity fluid;
        public final long amount;
        public final List<FluidAlternative> alternatives;

        public FluidPart(int slot, FluidIdentity fluid, long amount, int chance, List<FluidAlternative> alternatives) {
            checkPart(slot, amount, chance);
            this.slot = slot;
            this.fluid = Objects.requireNonNull(fluid, "Fluid identity");
            this.amount = amount;
            this.chance = chance;
            this.alternatives = copy(alternatives, MAX_ALTERNATIVES);
        }
    }

    public final String adapter, map, fingerprint;
    public final long eut;
    public final int duration, amperage;
    public final boolean hidden, needsEmptyOutput;
    public final List<ItemPart> itemInputs, itemOutputs;
    public final List<FluidPart> fluidInputs, fluidOutputs;
    /**
     * Stable requirement codes, not arbitrary native objects' display strings. Empty does not prove machine
     * feasibility.
     */
    public final List<String> unsupportedRequirements;
    private final long retainedBytes;

    public RecipeDescriptor(String adapter, String map, long eut, int duration, int amperage, boolean hidden,
        boolean needsEmptyOutput, List<ItemPart> itemInputs, List<ItemPart> itemOutputs, List<FluidPart> fluidInputs,
        List<FluidPart> fluidOutputs, List<String> unsupportedRequirements, QueryBudget budget) {
        Objects.requireNonNull(budget, "Recipe construction budget");
        budget.chargeAllocation(32);
        this.adapter = text(adapter, false);
        this.map = text(map, false);
        if (eut < 0 || duration <= 0 || amperage < 1 || amperage > 64) throw invalid("Unsupported processing costs");
        this.eut = eut;
        this.duration = duration;
        this.amperage = amperage;
        this.hidden = hidden;
        this.needsEmptyOutput = needsEmptyOutput;
        this.itemInputs = copy(itemInputs, MAX_SLOTS);
        this.itemOutputs = copy(itemOutputs, MAX_SLOTS);
        this.fluidInputs = copy(fluidInputs, MAX_SLOTS);
        this.fluidOutputs = copy(fluidOutputs, MAX_SLOTS);
        List<String> requirements = new ArrayList<>(copy(unsupportedRequirements, MAX_REQUIREMENTS));
        for (String requirement : requirements) text(requirement, false);
        budget.chargeSlots(requirements.size() * requirements.size() * 128);
        Collections.sort(requirements);
        this.unsupportedRequirements = Collections.unmodifiableList(requirements);
        checkItemOrder(this.itemInputs, false);
        checkItemOrder(this.itemOutputs, true);
        checkFluidOrder(this.fluidInputs, false);
        checkFluidOrder(this.fluidOutputs, true);
        retainedBytes = measure();
        fingerprint = fingerprint(Objects.requireNonNull(budget, "Recipe fingerprint budget"));
    }

    public long retainedBytes() {
        return retainedBytes;
    }

    private static void checkPart(int slot, long amount, int chance) {
        if (slot < 0 || slot >= 256 || amount < 0 || chance < 0 || chance > 10000)
            throw invalid("Invalid recipe slot, quantity or chance");
    }

    private static void checkItemOrder(List<ItemPart> parts, boolean output) {
        int previous = -1;
        for (ItemPart part : parts) {
            if (part.slot <= previous || output && (part.amount == 0 || part.matching != Matching.EXACT
                || !part.alternatives.isEmpty()
                || !part.displayAlternatives.isEmpty()
                || part.nbtSensitive)) throw invalid("Invalid ordered item slots");
            previous = part.slot;
        }
    }

    private static void checkFluidOrder(List<FluidPart> parts, boolean output) {
        int previous = -1;
        for (FluidPart part : parts) {
            if (part.slot <= previous || output && (part.amount == 0 || !part.alternatives.isEmpty()))
                throw invalid("Invalid ordered fluid slots");
            previous = part.slot;
        }
    }

    private long measure() {
        long bytes = 1024 + 2L * (adapter.length() + map.length());
        for (List<ItemPart> parts : java.util.Arrays.asList(itemInputs, itemOutputs)) for (ItemPart part : parts) {
            bytes = Math.addExact(bytes, 256L + part.item.getRetainedOpaqueBytes() + 2L * part.ore.length());
            for (ItemIdentity key : part.alternatives) bytes = Math.addExact(bytes, 16L + key.getRetainedOpaqueBytes());
            for (ItemIdentity key : part.displayAlternatives)
                bytes = Math.addExact(bytes, 16L + key.getRetainedOpaqueBytes());
        }
        for (List<FluidPart> parts : java.util.Arrays.asList(fluidInputs, fluidOutputs)) for (FluidPart part : parts) {
            bytes = Math.addExact(
                bytes,
                320L + part.fluid.getRetainedOpaqueBytes()
                    + 2L * part.fluid.getId()
                        .length());
            for (FluidAlternative alternative : part.alternatives) bytes = Math.addExact(
                bytes,
                128L + alternative.fluid.getRetainedOpaqueBytes()
                    + 2L * alternative.fluid.getId()
                        .length());
        }
        for (String requirement : unsupportedRequirements)
            bytes = Math.addExact(bytes, 64L + 2L * requirement.length());
        return bytes;
    }

    private String fingerprint(QueryBudget budget) {
        try {
            budget.chargeAllocation(512);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            DataOutputStream encoded = new DataOutputStream(new DigestOutputStream(new OutputStream() {

                @Override
                public void write(int value) {}

                @Override
                public void write(byte[] value, int offset, int length) {}
            }, digest));
            // Versioned domain; every variable-width field has an explicit count/length.
            string(encoded, "SFM-RECIPE-1", budget);
            string(encoded, adapter, budget);
            string(encoded, map, budget);
            budget.chargeSlots(18);
            encoded.writeLong(eut);
            encoded.writeInt(duration);
            encoded.writeInt(amperage);
            encoded.writeBoolean(hidden);
            encoded.writeBoolean(needsEmptyOutput);
            items(encoded, itemInputs, budget);
            items(encoded, itemOutputs, budget);
            fluids(encoded, fluidInputs, budget);
            fluids(encoded, fluidOutputs, budget);
            budget.chargeSlots(4);
            encoded.writeInt(unsupportedRequirements.size());
            for (String requirement : unsupportedRequirements) string(encoded, requirement, budget);
            encoded.flush();
            StringBuilder hex = new StringBuilder(64);
            for (byte value : digest.digest()) {
                hex.append(Character.forDigit((value >>> 4) & 15, 16));
                hex.append(Character.forDigit(value & 15, 16));
            }
            return hex.toString();
        } catch (IOException | NoSuchAlgorithmException failure) {
            throw new IllegalStateException("Recipe fingerprint unavailable", failure);
        }
    }

    private static void items(DataOutputStream out, List<ItemPart> parts, QueryBudget budget) throws IOException {
        budget.chargeSlots(4);
        out.writeInt(parts.size());
        for (ItemPart part : parts) {
            budget.chargeSlots(18);
            out.writeInt(part.slot);
            out.writeLong(part.amount);
            out.writeInt(part.chance);
            out.writeByte(part.matching.encoding);
            out.writeBoolean(part.nbtSensitive);
            budget.chargeSlots(32);
            out.write(part.item.fingerprint(budget));
            string(out, part.ore, budget);
            for (List<ItemIdentity> alternatives : java.util.Arrays
                .asList(part.alternatives, part.displayAlternatives)) {
                budget.chargeSlots(4);
                out.writeInt(alternatives.size());
                for (ItemIdentity key : alternatives) {
                    budget.chargeSlots(32);
                    out.write(key.fingerprint(budget));
                }
            }
        }
    }

    private static void fluids(DataOutputStream out, List<FluidPart> parts, QueryBudget budget) throws IOException {
        budget.chargeSlots(4);
        out.writeInt(parts.size());
        for (FluidPart part : parts) {
            budget.chargeSlots(20);
            out.writeInt(part.slot);
            out.writeLong(part.amount);
            out.writeInt(part.chance);
            budget.chargeSlots(32);
            out.write(part.fluid.fingerprint(budget));
            out.writeInt(part.alternatives.size());
            for (FluidAlternative alternative : part.alternatives) {
                budget.chargeSlots(40);
                out.writeLong(alternative.amount);
                out.write(alternative.fluid.fingerprint(budget));
            }
        }
    }

    private static void string(DataOutputStream out, String value, QueryBudget budget) throws IOException {
        budget.chargeSlots(4 + value.length() * 2);
        out.writeInt(value.length());
        out.writeChars(value);
    }

    private static <T> List<T> copy(List<T> values, int max) {
        if (values == null || values.size() > max) throw invalid("Recipe collection limit exceeded");
        for (T value : values) Objects.requireNonNull(value, "Recipe entry");
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    static String text(String value, boolean empty) {
        if (value == null || value.length() > 128 || !empty && value.isEmpty())
            throw invalid("Invalid recipe identifier");
        for (int i = 0; i < value.length(); i++)
            if (value.charAt(i) < 32 || value.charAt(i) == 127) throw invalid("Control character in recipe identifier");
        return value;
    }

    private static IllegalArgumentException invalid(String reason) {
        return new IllegalArgumentException(reason);
    }
}
