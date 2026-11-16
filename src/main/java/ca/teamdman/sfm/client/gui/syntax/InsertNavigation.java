package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Card;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Form;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Match;

/** Single-category navigation. Topics cross-link cards; they never become hidden intersecting filters. */
public final class InsertNavigation {

    public enum Section {

        TRIGGERS("Program flow", "Timers & events", "Choose when a program runs.", "triggers", ""),
        CONDITIONS("Program flow", "Decisions & checks", "Choose what happens when a condition is true or false.",
            "conditions", ""),
        LOOPS("Program flow", "Loops & waiting", "Repeat instructions, leave a loop, or wait.", "loops", ""),
        VARIABLES("Program flow", "Variables & memory", "Store values locally or between program runs.", "variables",
            ""),
        FUNCTIONS("Program flow", "Functions", "Define reusable code, call functions and return values.", "functions",
            ""),
        ITEMS("Resources", "Items", "Move items and inspect their quantities or contents.", "", "items"),
        FLUIDS("Resources", "Fluids", "Move and inspect fluids. Amounts use millibuckets (mB).", "", "fluids"),
        REDSTONE("Resources", "Redstone", "React to input, check a signal, or control an output.", "", "redstone"),
        CONTAINERS("Resources", "Containers & labels",
            "Select connected blocks, faces and slots; inspect availability or contents.", "", ""),
        ARRAYS("Data", "Lists & records", "Create arrays and structs, read members and change their contents.", "",
            "arrays"),
        TEXT("Data", "Text", "Create, join and inspect strings.", "", "text"),
        MATH("Data", "Math & logic", "Arithmetic, comparisons, Boolean logic and bitwise operations.", "", ""),
        NBT("Data", "Item NBT", "Inspect custom item data such as heat, charge and stored settings.", "", ""),
        MACHINES("Tools", "Machines & recipes", "Set machine options and inspect recipe information.", "", ""),
        DEBUG("Tools", "Debug output", "Print values and messages to your debug terminal.", "debug", ""),
        TEMPLATES("Start a program", "Program templates",
            "Complete programs. Open a new Studio draft while preserving your current work.", "", "");

        public final String group, name, description, role, topic;

        Section(String group, String name, String description, String role, String topic) {
            this.group = group;
            this.name = name;
            this.description = description;
            this.role = role;
            this.topic = topic;
        }

        public boolean includes(Card card) {
            if (this == TEMPLATES) return wholeProgram(card);
            if (wholeProgram(card)) return false;
            switch (this) {
                case CONTAINERS:
                    return card.role.equals("selection") || hasTopic(card, "containers");
                case ARRAYS:
                    return hasTopic(card, "arrays") || card.id.contains("struct") || card.id.contains("record");
                case MATH:
                    return card.role.equals("calculate") || card.id.equals("math_compare")
                        || Arrays.asList("cond_not", "cond_and", "cond_or")
                            .contains(card.id);
                case NBT:
                    return (card.id + " " + card.name).toLowerCase(Locale.ROOT)
                        .contains("nbt");
                case MACHINES:
                    return hasTopic(card, "machines") || hasTopic(card, "recipes");
                default:
                    return !role.isEmpty() ? role.equals(card.role) : hasTopic(card, topic);
            }
        }
    }

    private static final List<String> PRIORITY = Arrays.asList(
        "functions_transfer",
        "functions_transferf",
        "io_basic",
        "io_input_all",
        "io_output_all",
        "methods_empty",
        "methods_fempty",
        "methods_count",
        "methods_contents",
        "trig_timer",
        "trig_port_rising",
        "functions_pulse");

    private InsertNavigation() {}

    public static boolean wholeProgram(Card card) {
        for (Form form : card.forms) if (form.placement.equals("New program")) return true;
        return false;
    }

    private static boolean hasTopic(Card card, String topic) {
        for (Form form : card.forms) if (form.topics.contains(topic)) return true;
        return false;
    }

    public static List<Form> forms(Card card, Section section, String query) {
        if (!query.trim()
            .isEmpty() || section.topic.isEmpty()) return card.forms;
        List<Form> matching = new ArrayList<>();
        for (Form form : card.forms) if (form.topics.contains(section.topic)) matching.add(form);
        return matching.isEmpty() ? card.forms : Collections.unmodifiableList(matching);
    }

    public static List<Match> search(List<Card> cards, Section section, String query) {
        boolean global = !query.trim()
            .isEmpty();
        List<Card> scope = new ArrayList<>();
        for (Card card : cards) if (global || section.includes(card)) scope.add(card);
        List<Match> matches = InsertCatalog.search(scope, "", global ? "" : section.topic, 2, query);
        matches.sort((a, b) -> Integer.compare(priority(a.card), priority(b.card)));
        return matches;
    }

    private static int priority(Card card) {
        int index = PRIORITY.indexOf(card.id);
        return index < 0 ? PRIORITY.size() : index;
    }

    public static Section sectionFor(Card card, Section preferred) {
        if (preferred.includes(card)) return preferred;
        for (Section section : Section.values()) if (section.includes(card)) return section;
        throw new IllegalArgumentException("Insert has no category: " + card.id);
    }

    public static String kind(String role) {
        switch (role) {
            case "triggers":
                return "Trigger";
            case "conditions":
                return "Check";
            case "loops":
                return "Loop / wait";
            case "transfers":
                return "Action";
            case "selection":
                return "Selection / filter";
            case "variables":
                return "Value / setup";
            case "functions":
                return "Function";
            case "read":
                return "Read value";
            case "calculate":
                return "Calculation";
            case "debug":
                return "Debug";
            default:
                throw new IllegalArgumentException(role);
        }
    }
}
