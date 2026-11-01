package ca.teamdman.sfm.client.gui.mui2;

/** Complete local starting points. Choosing a template creates a draft; it never changes a manager. */
public final class StudioTemplates {

    public enum Template {

        TRANSFER("Item transfer", "EVERY 20 TICKS DO\n  INPUT FROM \"source\"\n  OUTPUT TO \"destination\"\nEND\n"),
        GATE("Stock gate",
            "EVERY 20 TICKS DO\n  IF \"source\" HAS >= 1 THEN\n    INPUT FROM \"source\"\n    OUTPUT TO \"destination\"\n  END\nEND\n"),
        PULSE("Redstone pulse", "EVERY REDSTONE PULSE DO\n  INPUT FROM \"source\"\n  OUTPUT TO \"destination\"\nEND\n"),
        LIBRARY("Function library", "integer twice(integer value) {\n  return value * 2;\n}\n");

        public final String title, source;

        Template(String title, String source) {
            this.title = title;
            this.source = source;
        }
    }

    private StudioTemplates() {}
}
