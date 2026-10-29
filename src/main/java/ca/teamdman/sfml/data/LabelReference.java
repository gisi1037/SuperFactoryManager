package ca.teamdman.sfml.data;

import java.util.Locale;

/** A name in the caller's label snapshot, never a captured world or inventory. */
public final class LabelReference {

    private static final String[] SIDES = { "down", "up", "north", "south", "west", "east" };
    private final String name;
    private final int sideMask, firstSlot, lastSlot;
    private final boolean slotSelection;

    private LabelReference(String name) {
        this(name, 63, 0, Integer.MAX_VALUE, false);
    }

    private LabelReference(String name, int sideMask, int firstSlot, int lastSlot, boolean slotSelection) {
        this.name = name;
        this.sideMask = sideMask;
        this.firstSlot = firstSlot;
        this.lastSlot = lastSlot;
        this.slotSelection = slotSelection;
    }

    public static LabelReference literal(String name) {
        if (name == null || name.isEmpty() || name.length() > 256 || !name.equals(name.trim()))
            throw new IllegalArgumentException(
                "LABEL needs an exact nonempty name of at most 256 characters without surrounding whitespace");
        DataValue.string(name); // Validate Unicode once, while compiling the literal.
        return new LabelReference(name);
    }

    public String getName() {
        return name;
    }

    public int getSideMask() {
        return sideMask;
    }

    public int getFirstSlot() {
        return firstSlot;
    }

    public int getLastSlot() {
        return lastSlot;
    }

    public boolean hasSlotSelection() {
        return slotSelection;
    }

    /** Absolute physical sides, in the vanilla direction order; no manager-relative faces. */
    public static int sideIndex(String side) {
        if (side != null) switch (side.toLowerCase(Locale.ROOT)) {
            case "bottom":
            case "down":
                return 0;
            case "top":
            case "up":
                return 1;
            case "north":
                return 2;
            case "south":
                return 3;
            case "west":
                return 4;
            case "east":
                return 5;
        }
        throw new IllegalArgumentException("side requires down, up, north, south, west or east (top/bottom aliases)");
    }

    /** Each modifier replaces its own restriction and preserves the other restriction. */
    public LabelReference side(int side) {
        if (side < 0 || side >= SIDES.length) throw new IllegalArgumentException("Invalid absolute query side");
        return new LabelReference(name, 1 << side, firstSlot, lastSlot, slotSelection);
    }

    public LabelReference slots(long first, long last) {
        if (first < 0 || last < first || last > Integer.MAX_VALUE)
            throw new IllegalArgumentException("slots requires 0 <= first <= last <= 2147483647 (inclusive)");
        return new LabelReference(name, sideMask, (int) first, (int) last, true);
    }

    @Override
    public String toString() {
        String result = "LABEL(" + DataValue.string(name) + ")";
        if (sideMask != 63) result += ".side(\"" + SIDES[Integer.numberOfTrailingZeros(sideMask)] + "\")";
        if (slotSelection) result += ".slots(" + firstSlot + ", " + lastSlot + ")";
        return result;
    }
}
