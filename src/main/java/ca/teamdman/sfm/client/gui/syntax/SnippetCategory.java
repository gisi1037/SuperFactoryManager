package ca.teamdman.sfm.client.gui.syntax;

public enum SnippetCategory {

    TRIGGER("⚡ Triggers", 0xDCDCAA),
    IO("📦 I/O Transfer", 0x4EC9B0),
    CONDITION("❓ Logic & If", 0xC586C0),
    LABEL("🏷️ Labels", 0x569CD6),
    MODIFIER("⚙️ Modifiers", 0xD7BA7D);

    private final String displayName;
    private final int color;

    SnippetCategory(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }
}
