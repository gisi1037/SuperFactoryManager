package ca.teamdman.sfm.client.gui.syntax;

public enum SnippetCategory {

    TRIGGER("Triggers", "Timers, global intervals and named Redstone Port edges", 0xDCDCAA),
    IO("I/O", "Item transfers, retention, redstone output and FORGET", 0x4EC9B0),
    CONDITION("Logic", "IF branches, FOR/WHILE loops, HAS and boolean conditions", 0xC586C0),
    LABEL("Labels", "Disk labels, block groups and absolute sides", 0x569CD6),
    MODIFIER("Modifiers", "Slot ranges, item filters, EACH and round robin", 0xD7BA7D),
    DATA("Data", "LET, STATE, SET, PRINT, array iteration and computed quantities", 0x9CDCFE),
    COLLECTION("Arrays", "Fixed arrays, structs, nested reads and updates", 0x4EC9B0),
    STRING("Strings", "String literals, concatenation, equality and escapes", 0xCE9178),
    MATH("Math", "Integer arithmetic, comparisons and 64-bit bitwise operators", 0xB5CEA8),
    QUERY("Queries", "Typed item observations and native Redstone Port input queries", 0xDCDCAA);

    private final String displayName;
    private final int color;
    private final String description;

    SnippetCategory(String displayName, String description, int color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }
}
