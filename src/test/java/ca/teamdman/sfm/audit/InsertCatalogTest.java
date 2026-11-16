package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.mui2.InsertBrowser;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Card;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Form;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Match;
import ca.teamdman.sfm.client.gui.syntax.InsertPlacement;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog;
import ca.teamdman.sfm.client.gui.syntax.SyntaxRegistry;
import ca.teamdman.sfm.client.gui.syntax.SyntaxSnippet;

class InsertCatalogTest {

    private final InsertCatalog catalog = InsertCatalog.INSTANCE;

    @Test
    void packagedCatalogCoversEveryExistingInsertAndBuiltinReference() throws Exception {
        try (java.io.Reader reader = new java.io.InputStreamReader(
            getClass().getResourceAsStream("/assets/superfactorymanager/editor/insert-catalog.json"),
            java.nio.charset.StandardCharsets.UTF_8)) {
            com.google.gson.JsonObject data = new com.google.gson.JsonParser().parse(reader)
                .getAsJsonObject();
            com.google.gson.JsonObject aliases = data.getAsJsonObject("legacyAliasMap");
            for (SyntaxSnippet snippet : SyntaxRegistry.getAllSnippets()) {
                assertTrue(aliases.has(snippet.getId()), snippet.getId());
                assertNotNull(
                    catalog.card(
                        aliases.get(snippet.getId())
                            .getAsString()),
                    snippet.getId());
            }
            Set<String> references = new HashSet<>();
            for (com.google.gson.JsonElement entry : data.getAsJsonArray("entries")) {
                com.google.gson.JsonElement source = entry.getAsJsonObject()
                    .get("source");
                if (source != null && source.isJsonPrimitive()) references.add(source.getAsString());
            }
            for (SfmlCompletionCatalog.Entry builtin : SfmlCompletionCatalog.builtIns()) {
                String name = builtin.getName()
                    .toLowerCase(java.util.Locale.ROOT);
                String key = (builtin.getKind() == SfmlCompletionCatalog.Kind.METHOD ? "methods:" : "functions:")
                    + name;
                if (references.contains(key)) continue;
                assertTrue(aliases.has(key), key);
                Card card = catalog.card(
                    aliases.get(key)
                        .getAsString());
                assertNotNull(card, key);
                assertTrue(
                    card.forms.stream()
                        .anyMatch(form -> form.code.startsWith(name + "(")),
                    key);
            }
        }
    }

    @Test
    void completeCatalogHasTwoAxesUniqueVisibleCardsAndValidRelatedTargets() {
        assertEquals(10, catalog.roles.size());
        assertEquals(10, catalog.topics.size());
        assertEquals(147, catalog.cards.size());
        Set<String> names = new HashSet<>(), ids = new HashSet<>();
        int examples = 0;
        for (Card card : catalog.cards) {
            assertTrue(names.add(card.name), card.name);
            assertTrue(ids.add(card.id));
            assertNotNull(catalog.role(card.role));
            if (card.example) examples++;
            for (String related : card.related) assertNotNull(catalog.card(related), related);
            assertFalse(card.forms.isEmpty());
            for (Form form : card.forms) assertFalse(form.placement.isEmpty(), card.id);
        }
        assertEquals(10, examples);
        assertNull(catalog.card("functions_break"));
        assertFalse(catalog.firstSteps.isEmpty());
        assertTrue(catalog.glossary.containsKey("Label"));
    }

    @Test
    void mixedRedstoneTopicRetainsDistinctActionColorsAndIncludesPulseOutput() {
        Set<String> roles = new HashSet<>();
        for (Match match : InsertCatalog.search(catalog.cards, "", "redstone", 0, "")) roles.add(match.card.role);
        assertTrue(roles.containsAll(Arrays.asList("triggers", "conditions", "read", "transfers")));
        assertNotEquals(catalog.role("triggers").color, catalog.role("transfers").color);
        assertEquals("transfers", catalog.card("functions_pulse").role);
        assertTrue(catalog.card("functions_pulse").forms.get(0).topics.contains("redstone"));
    }

    @Test
    void axesIntersectAndQueriesRequireEveryWordWithoutFallback() {
        assertTrue(
            InsertCatalog.search(catalog.cards, "debug", "fluids", 0, "")
                .isEmpty());
        assertTrue(
            InsertCatalog.search(catalog.cards, "", "", 0, "no_such_feature_123")
                .isEmpty());
        for (Match match : InsertCatalog.search(catalog.cards, "read", "items", 0, "")) {
            assertEquals("read", match.card.role);
            assertTrue(match.form.topics.contains("items"));
        }
        assertFalse(
            InsertCatalog.search(catalog.cards, "", "", 0, "pulse timed")
                .isEmpty());
        assertTrue(
            InsertCatalog.search(catalog.cards, "", "", 0, "pulse no_such_feature")
                .isEmpty());
    }

    @Test
    void queryAndTopicChooseTheRelevantVariantRatherThanAnUnrelatedDefault() {
        Match divide = InsertCatalog.search(catalog.cards, "calculate", "numbers", 0, "divide")
            .stream()
            .filter(m -> m.card.id.equals("math_arithmetic"))
            .findFirst()
            .orElseThrow(AssertionError::new);
        assertTrue(
            divide.form.name.toLowerCase(java.util.Locale.ROOT)
                .contains("divid"));
        Match fluid = InsertCatalog.search(catalog.cards, "transfers", "fluids", 0, "keep stock")
            .stream()
            .filter(m -> m.card.id.equals("io_input_retain"))
            .findFirst()
            .orElseThrow(AssertionError::new);
        assertTrue(fluid.form.code.contains("fluid:water"));
        assertTrue(
            fluid.form.field("inputs")
                .contains("mB"));
        assertFalse(
            fluid.form.field("inputs")
                .contains("item count"));
    }

    @Test
    void examplesRemainSeparateAndResultCaptureAddsNoHiddenActions() {
        for (Match match : InsertCatalog.search(catalog.cards, "", "", 1, "")) assertTrue(match.card.example);
        for (Match match : InsertCatalog.search(catalog.cards, "", "", 0, "")) assertFalse(match.card.example);
        Form transfer = catalog.card("functions_transfer").forms.get(0);
        String captured = transfer.code(true, ">=");
        assertTrue(captured.contains(" into <new_result_name>"));
        assertFalse(captured.contains("print("));
        assertEquals(transfer.code, transfer.code(false, ">="));
        Form condition = catalog.card("cond_has_one").forms.get(0);
        assertTrue(
            condition.code(false, "==")
                .contains("has =="));
    }

    @Test
    void playerFunctionsAndKnownLabelsKeepExactNamesAndEscapes() {
        java.util.List<Card> current = catalog.withContext(
            "integer Logistics.Find(integer count) {return count} every 20 ticks {}",
            Collections.singleton("箱\"Supply"));
        Card call = current.stream()
            .filter(c -> c.id.equals("call:Logistics.Find"))
            .findFirst()
            .orElseThrow(AssertionError::new);
        assertTrue(call.forms.get(0).code.startsWith("Logistics.Find("));
        assertEquals("Value", call.forms.get(0).placement);
        Card label = current.stream()
            .filter(c -> c.id.startsWith("label:"))
            .findFirst()
            .orElseThrow(AssertionError::new);
        assertTrue(label.forms.get(0).code.contains("\\\"Supply"));
    }

    @Test
    void placementUsesActualSyntaxAndSeparatesTriggersVoidAndValueFunctions() {
        Form timer = catalog.card("trig_timer").forms.get(0), pulse = catalog.card("functions_pulse").forms.get(0);
        assertFalse(
            at("every 20 ticks {|}").refusal(timer)
                .isEmpty());
        assertTrue(
            at("every 20 ticks {|}").refusal(pulse)
                .isEmpty());
        assertFalse(
            at("integer f() {| return 1 }").refusal(pulse)
                .isEmpty());
        assertTrue(
            at("void f() {|}").refusal(pulse)
                .isEmpty());
        assertFalse(
            at("|").refusal(pulse)
                .isEmpty());
        assertFalse(
            at("void f(){|}").refusal(catalog.card("cond_has_one").forms.get(0))
                .isEmpty());
        assertTrue(
            at("every 20 ticks {for i = 0 to 3 {|}}").refusal(catalog.card("break_loop").forms.get(0))
                .isEmpty());
        assertFalse(
            at("every 20 ticks {|}").refusal(catalog.card("break_loop").forms.get(0))
                .isEmpty());
        assertFalse(
            at("every 20 ticks {} |").refusal(catalog.card("data_state").forms.get(0))
                .isEmpty());
        Card procedure = catalog.withContext("void run() {}", Collections.emptySet())
            .stream()
            .filter(c -> c.id.equals("call:run"))
            .findFirst()
            .orElseThrow(AssertionError::new);
        assertFalse(
            at("integer f(){|return 1}").refusal(procedure.forms.get(0))
                .isEmpty());
    }

    @Test
    void unicodeOffsetsAndIncompleteOrExcessivelyNestedDraftsStaySafe() {
        assertTrue(
            at("name \"箱😀\" every 20 ticks {|}").refusal(catalog.card("functions_pulse").forms.get(0))
                .isEmpty());
        assertTrue(
            at("every unfinished |").note()
                .contains("uncertain"));
        String deep = String.join("", Collections.nCopies(500, "if(true){")) + "|";
        assertTrue(
            at(deep).note()
                .contains("uncertain"));
    }

    @Test
    void layoutFitsSmallScreensAndProvidesReadableWidePanes() {
        for (int width : new int[] { 320, 427, 640, 960, 1920 }) for (int height : new int[] { 240, 360, 540, 1080 }) {
            InsertBrowser.Geometry g = new InsertBrowser.Geometry(width, height);
            assertTrue(g.width <= width - 16);
            assertTrue(g.height <= height - 16);
            assertTrue(g.resultsX + g.resultsWidth <= g.width - 8);
            assertTrue(g.detailsX + g.detailsWidth <= g.width - 8);
            if (g.wide) {
                assertTrue(g.detailsWidth >= 300);
                assertTrue(g.resultsWidth >= 220);
            } else assertEquals(g.resultsX, g.detailsX);
        }
    }

    private static InsertPlacement at(String marked) {
        return InsertPlacement.at(marked.replace("|", ""), marked.indexOf('|'));
    }
}
