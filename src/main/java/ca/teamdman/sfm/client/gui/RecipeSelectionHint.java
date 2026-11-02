package ca.teamdman.sfm.client.gui;

import java.lang.ref.WeakReference;

import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.MachineRecipeMaps;
import ca.teamdman.sfml.data.QueryBudget;

/** One world-scoped, non-authoritative output hint. Never retains a stack, provider or recipe requirements. */
public final class RecipeSelectionHint {

    public static final RecipeSelectionHint NEI = new RecipeSelectionHint();

    public static final class Value {

        public final String item, map;
        public final int metadata;

        public Value(String item, int metadata, String map) {
            if (item == null || item.length() > 128 || !MachineRecipeMaps.qualified(map))
                throw new IllegalArgumentException(
                    "Only the qualified macerator, bender and cutter maps are supported");
            ItemIdentity.of(item, metadata, null, new QueryBudget());
            this.item = item;
            this.metadata = metadata;
            this.map = map;
        }
    }

    private WeakReference<Object> world = new WeakReference<>(null);
    private Value value;

    public void capture(Object owner, Value hint) {
        if (owner == null || hint == null) throw new IllegalArgumentException("A live client world is required");
        world = new WeakReference<>(owner);
        value = hint;
    }

    public Value current(Object owner) {
        if (owner == null || world.get() != owner) {
            value = null;
            world.clear();
        }
        return value;
    }

    /** The chosen fingerprint comes from a server-resolved row; a fresh query obtains runtime references. */
    public static String query(Value output, String fingerprint) {
        if (output == null || fingerprint == null || fingerprint.length() != 64)
            throw new IllegalArgumentException("Choose a server recipe candidate first");
        for (int i = 0; i < fingerprint.length(); i++) {
            char c = fingerprint.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f'))
                throw new IllegalArgumentException("Invalid recipe fingerprint");
        }
        return "-- Read-only recipe inspection; check machine requirements separately.\n"
            + "let found = recipes(itemkey(\""
            + output.item
            + "\", "
            + output.metadata
            + "), \""
            + output.map
            + "\")\n"
            + "let recipeChoiceFound = false\n"
            + "if(found.coverageComplete) {\n  for candidate in found.recipes {\n"
            + "    if(candidate.reference.fingerprint == \""
            + fingerprint
            + "\") {\n"
            + "      let details = recipe(candidate.reference)\n      print(details.status)\n      recipeChoiceFound = true\n    }\n  }\n}\n"
            + "if(!recipeChoiceFound) { print(\"Selected recipe unavailable; inspect again\", found.status) }";
    }
}
