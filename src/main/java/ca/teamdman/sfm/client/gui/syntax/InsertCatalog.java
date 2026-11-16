package ca.teamdman.sfm.client.gui.syntax;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** One immutable client catalog shared by both native editors. No world queries or per-frame parsing. */
public final class InsertCatalog {

    public static final InsertCatalog INSTANCE = load();
    public final List<Category> roles, topics;
    public final List<Card> cards;
    private final Map<String, Card> byId;
    public final List<String> firstSteps;
    public final Map<String, String> glossary;

    public static final class Category {

        public final String id, name, description;
        public final int color;

        Category(JsonObject data) {
            id = text(data, "id");
            name = text(data, "name");
            description = text(data, "description");
            String rgb = text(data, "color");
            color = rgb.isEmpty() ? 0xffb9c1cc : 0xff000000 | Integer.parseInt(rgb.substring(1), 16);
        }
    }

    public static final class Card {

        public final String id, name, role;
        public final boolean example;
        public final List<Form> forms;
        public final List<String> related;
        private final JsonObject data;
        private final String search;

        Card(JsonObject data) {
            this.data = data;
            id = text(data, "id");
            name = text(data, "name");
            role = text(data, "role");
            example = data.has("example") && data.get("example")
                .getAsBoolean();
            related = strings(data, "related");
            search = (name + " "
                + text(data, "summary")
                + " "
                + text(data, "details")
                + " "
                + text(data, "source")
                + " "
                + strings(data, "searchAliases")).toLowerCase(Locale.ROOT);
            List<Form> all = new ArrayList<>();
            if (data.has("variants") && data.getAsJsonArray("variants")
                .size() > 0) {
                for (JsonElement variant : data.getAsJsonArray("variants"))
                    all.add(new Form(this, variant.getAsJsonObject()));
            } else all.add(new Form(this, new JsonObject()));
            forms = Collections.unmodifiableList(all);
        }
    }

    public static final class Form {

        public final Card card;
        private final JsonObject data;
        public final String name, code, placement, group;
        public final List<String> topics;
        private final String search;

        Form(Card card, JsonObject data) {
            this.card = card;
            this.data = data;
            name = data.has("name") ? text(data, "name") : card.name;
            code = field("insert");
            placement = field("placement");
            group = text(data, "form");
            topics = strings(data.has("topics") ? data : card.data, "topics");
            search = (card.search + " " + name + " " + text(data, "summary") + " " + code).toLowerCase(Locale.ROOT);
        }

        public String field(String key) {
            return text(data.has(key) ? data : card.data, key);
        }

        public String code(boolean result, String comparison) {
            String value = code;
            if (card.data.has("variantAxes"))
                value = value.replaceAll("(?i)\\bhas\\s+(?:>=|<=|==|!=|=|>|<)", "has " + comparison);
            if (result && card.data.has("saveResultVariant"))
                value += text(card.data.getAsJsonObject("saveResultVariant"), "append");
            return value;
        }

        public boolean hasResult() {
            return card.data.has("saveResultVariant");
        }

        public boolean hasComparison() {
            return card.data.has("variantAxes");
        }

        public List<String> signatures() {
            return strings(data.has("signatures") ? data : card.data, "signatures");
        }

        public List<String> usage() {
            List<String> out = new ArrayList<>();
            if (card.data.has("usageExamples")) for (JsonElement e : card.data.getAsJsonArray("usageExamples")) {
                JsonObject guide = e.getAsJsonObject();
                out.add(text(guide, "title") + " (guide only; not inserted)");
                out.add(text(guide, "explanation"));
                out.add(text(guide, "placementHelp"));
                out.add(text(guide, "code"));
            }
            return out;
        }
    }

    public static final class Match {

        public final Card card;
        public final Form form;

        Match(Card card, Form form) {
            this.card = card;
            this.form = form;
        }
    }

    private InsertCatalog(JsonObject data) {
        roles = categories(data, "roles");
        topics = categories(data, "topics");
        firstSteps = strings(data.getAsJsonObject("firstSuccess"), "steps");
        Map<String, String> words = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> word : data.getAsJsonObject("glossary")
            .entrySet())
            words.put(
                word.getKey(),
                word.getValue()
                    .getAsString());
        glossary = Collections.unmodifiableMap(words);
        List<Card> entries = new ArrayList<>();
        Map<String, Card> indexed = new LinkedHashMap<>();
        for (JsonElement value : data.getAsJsonArray("entries")) {
            JsonObject entry = value.getAsJsonObject();
            if (entry.has("aliasOnly") && entry.get("aliasOnly")
                .getAsBoolean()) continue;
            Card card = new Card(entry);
            if (indexed.put(card.id, card) != null) throw new IllegalArgumentException("Duplicate insert: " + card.id);
            entries.add(card);
        }
        cards = Collections.unmodifiableList(entries);
        byId = Collections.unmodifiableMap(indexed);
    }

    public Card card(String id) {
        return byId.get(id);
    }

    public Category role(String id) {
        for (Category c : roles) if (c.id.equals(id)) return c;
        throw new IllegalArgumentException(id);
    }

    public String topicName(String id) {
        for (Category c : topics) if (c.id.equals(id)) return c.name;
        return "All topics";
    }

    public List<Card> withContext(String source, Set<String> labels) {
        List<Card> all = new ArrayList<>(cards);
        for (SfmlCompletionCatalog.Entry entry : SfmlCompletionCatalog.scan(source)
            .entries()) {
            if (entry.getKind() != SfmlCompletionCatalog.Kind.PLAYER_FUNCTION) continue;
            JsonObject data = dynamic(
                "call:" + entry.getName(),
                "Call " + entry.getName(),
                "functions",
                "program",
                entry.getTemplate(),
                entry.getDescription());
            data.addProperty(
                "details",
                entry.getSignature() + ". Exact declared names and parameter order are preserved.");
            data.addProperty(
                "placement",
                entry.getSignature()
                    .matches("(?is).*->\\s*void\\s*$") ? "Inside running code" : "Value");
            all.add(new Card(data));
        }
        int count = 0;
        for (String label : labels) {
            if (count++ == 256) break;
            JsonObject data = dynamic(
                "label:" + label,
                "Select label: " + label,
                "selection",
                "containers",
                "label(" + ca.teamdman.sfml.data.DataValue.string(label) + ")",
                "Select the connected blocks named " + label + ".");
            all.add(new Card(data));
        }
        return Collections.unmodifiableList(all);
    }

    /** view 0 = building blocks, 1 = worked examples, 2 = explicit Search all. Axes always intersect. */
    public static List<Match> search(List<Card> cards, String action, String topic, int view, String query) {
        String trimmed = query == null ? ""
            : query.trim()
                .toLowerCase(Locale.ROOT);
        String[] words = trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
        List<Match> results = new ArrayList<>();
        for (Card card : cards) {
            if (!action.isEmpty() && !action.equals(card.role) || view != 2 && card.example != (view == 1)) continue;
            Form chosen = null;
            int best = -1;
            for (Form form : card.forms) {
                if (!topic.isEmpty() && !form.topics.contains(topic)) continue;
                boolean matches = true;
                int score = 0;
                for (String word : words) {
                    if (!form.search.contains(word)) {
                        matches = false;
                        break;
                    }
                    if (form.name.toLowerCase(Locale.ROOT)
                        .contains(word)) score++;
                }
                if (matches && score > best) {
                    chosen = form;
                    best = score;
                }
            }
            if (chosen != null) results.add(new Match(card, chosen));
        }
        return results;
    }

    private static JsonObject dynamic(String id, String name, String role, String topic, String code, String summary) {
        JsonObject data = new JsonObject();
        data.addProperty("id", id);
        data.addProperty("name", name);
        data.addProperty("role", role);
        data.addProperty("insert", code);
        data.addProperty("summary", summary);
        data.addProperty("placement", "Value");
        com.google.gson.JsonArray topics = new com.google.gson.JsonArray();
        topics.add(new com.google.gson.JsonPrimitive(topic));
        data.add("topics", topics);
        return data;
    }

    private static List<Category> categories(JsonObject data, String field) {
        List<Category> values = new ArrayList<>();
        for (JsonElement e : data.getAsJsonArray(field)) values.add(new Category(e.getAsJsonObject()));
        return Collections.unmodifiableList(values);
    }

    static String text(JsonObject data, String field) {
        return data.has(field) && data.get(field)
            .isJsonPrimitive() ? data.get(field)
                .getAsString() : "";
    }

    static List<String> strings(JsonObject data, String field) {
        List<String> values = new ArrayList<>();
        if (data.has(field)) for (JsonElement e : data.getAsJsonArray(field)) values.add(e.getAsString());
        return Collections.unmodifiableList(values);
    }

    private static InsertCatalog load() {
        try (InputStreamReader reader = new InputStreamReader(
            InsertCatalog.class.getResourceAsStream("/assets/superfactorymanager/editor/insert-catalog.json"),
            StandardCharsets.UTF_8)) {
            return new InsertCatalog(
                new JsonParser().parse(reader)
                    .getAsJsonObject());
        } catch (Exception invalid) {
            throw new IllegalStateException("Cannot load the SFML insert catalog", invalid);
        }
    }
}
