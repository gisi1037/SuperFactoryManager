package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Context;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Entry;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Kind;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.OutlineKind;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Snapshot;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlCompletionCatalogTest {

    @TestFactory
    List<DynamicTest> everyBuiltInExamplePassesTheActualCompiler() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Entry entry : SfmlCompletionCatalog.builtIns()) {
            tests.add(DynamicTest.dynamicTest(entry.getName(), () -> {
                String source = "every 20 ticks do\n" + entry.getExample() + "\nend";
                ProgramBuildResult result = new ProgramBuilder(source).build();
                assertTrue(result.isSuccess(), result.getErrorMessage() + "\n" + source);
                assertFalse(
                    entry.getSignature()
                        .isEmpty());
                assertFalse(
                    entry.getDescription()
                        .isEmpty());
                assertFalse(
                    entry.getTemplate()
                        .isEmpty());
            }));
        }
        return tests;
    }

    @Test
    void nativeCatalogAndExistingCompilerCheckedVscodeReferenceListTheSameApi() throws Exception {
        JsonObject reference = new JsonParser()
            .parse(
                new String(
                    Files.readAllBytes(Paths.get("editors/vscode-sfml/data/functions.json")),
                    StandardCharsets.UTF_8))
            .getAsJsonObject();
        Set<String> expected = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : reference.getAsJsonObject("functions")
            .entrySet()) expected.add("function:" + entry.getKey());
        for (Map.Entry<String, JsonElement> entry : reference.getAsJsonObject("methods")
            .entrySet()) expected.add("method:" + entry.getKey());
        Set<String> actual = new HashSet<>();
        for (Entry entry : SfmlCompletionCatalog.builtIns()) {
            String key = (entry.getKind() == Kind.METHOD ? "method:" : "function:") + entry.getName()
                .toLowerCase(Locale.ROOT);
            assertTrue(actual.add(key), key);
            if (entry.getKind() == Kind.METHOD) assertEquals(Context.RECEIVER, entry.getContext());
            else if (entry.getKind() == Kind.STATEMENT) assertEquals(Context.TRIGGER_STATEMENT, entry.getContext());
            else assertEquals(Context.EXPRESSION, entry.getContext());
        }
        assertEquals(expected, actual);
        assertEquals(22, actual.size());
    }

    @Test
    void currentFunctionsRetainExactCaseNamespacesAndNestedRecordTypes() {
        String source = "-- function fake() returns integer do\n"
            + "let $text = \"function AlsoFake() returns integer do\"\n"
            + "FuNcTiOn Logistics.Next($Stock: {item: ITEM_KEY, total: INTEGER}[2], $Want: INTEGER) "
            + "returns {Moved: INTEGER, Ready: BOOLEAN} do return {Moved: $Want, Ready: true} end\n"
            + "function empty() returns boolean do return true end\n"
            + "every 20 ticks do print(\"every redstone pulse do\") end";
        Snapshot snapshot = SfmlCompletionCatalog.scan(source);
        List<Entry> players = players(snapshot);
        assertEquals(2, players.size());
        Entry first = players.get(0);
        assertEquals("Logistics.Next", first.getName());
        assertEquals("Logistics.Next(<Stock>, <Want>)", first.getTemplate());
        assertTrue(
            first.getSignature()
                .contains("ITEM_KEY"));
        assertTrue(
            first.getSignature()
                .contains("Moved: INTEGER"));
        assertEquals(
            3,
            snapshot.outline()
                .size());
        assertEquals(
            OutlineKind.FUNCTION,
            snapshot.outline()
                .get(0)
                .getKind());
        assertEquals(
            OutlineKind.TRIGGER,
            snapshot.outline()
                .get(2)
                .getKind());
        assertEquals(
            3,
            snapshot.outline()
                .get(0)
                .getLine());
        assertEquals(
            source.indexOf("FuNcTiOn"),
            snapshot.outline()
                .get(0)
                .getOffset());
        assertFalse(snapshot.isTruncated());
    }

    @Test
    void commentsInsideSignaturesAreExcludedAndIncompleteHeadersAreIgnored() {
        Snapshot snapshot = SfmlCompletionCatalog.scan(
            "function real( -- ignored($fake: BOOLEAN)\n" + "$value: INTEGER) returns INTEGER do return $value end\n"
                + "function incomplete($other: INTEGER) returns INTEGER");
        assertEquals(1, players(snapshot).size());
        assertEquals(
            "real(<value>)",
            players(snapshot).get(0)
                .getTemplate());
        assertFalse(
            players(snapshot).get(0)
                .getSignature()
                .contains("ignored"));
        assertEquals(
            1,
            snapshot.outline()
                .size());
    }

    @Test
    void exactNamePlayerFunctionPrecedenceMatchesTheCompiler() {
        Snapshot snapshot = SfmlCompletionCatalog.scan(
            "function min($value: INTEGER) returns INTEGER do return $value end "
                + "function MAX($value: INTEGER) returns INTEGER do return $value end");
        assertEquals(
            1,
            snapshot.entries()
                .stream()
                .filter(
                    e -> e.getName()
                        .equals("min"))
                .count());
        assertEquals(
            Kind.PLAYER_FUNCTION,
            snapshot.entries()
                .stream()
                .filter(
                    e -> e.getName()
                        .equals("min"))
                .findFirst()
                .get()
                .getKind());
        assertTrue(
            snapshot.entries()
                .stream()
                .anyMatch(
                    e -> e.getName()
                        .equals("max") && e.getKind() == Kind.FUNCTION));
        assertTrue(
            snapshot.entries()
                .stream()
                .anyMatch(
                    e -> e.getName()
                        .equals("MAX") && e.getKind() == Kind.PLAYER_FUNCTION));
    }

    @Test
    void unicodeBeforeOutlineTargetsUsesJavaEditorOffsets() {
        String source = "name \"Factory \uD83C\uDFED\"\nevery 20 ticks do end";
        Snapshot snapshot = SfmlCompletionCatalog.scan(source);
        assertEquals(
            source.indexOf("every"),
            snapshot.outline()
                .get(0)
                .getOffset());
        assertEquals(
            2,
            snapshot.outline()
                .get(0)
                .getLine());
    }

    @Test
    void searchIsBoundedCaseInsensitiveAndDoesNotRewriteTemplatesOrStrings() {
        Snapshot snapshot = SfmlCompletionCatalog.scan(null);
        assertEquals(
            1,
            SfmlCompletionCatalog.search(snapshot, "ABS integer", 30)
                .size());
        assertEquals(
            "abs",
            SfmlCompletionCatalog.search(snapshot, "ABS integer", 30)
                .get(0)
                .getName());
        assertTrue(
            SfmlCompletionCatalog.search(snapshot, "certainly-not-a-real-function", 10)
                .isEmpty());
        assertEquals(
            3,
            SfmlCompletionCatalog.search(snapshot, "", 3)
                .size());
        assertTrue(
            SfmlCompletionCatalog.search(snapshot, "", 0)
                .isEmpty());
        assertEquals(
            "item(\"<namespace:item>\")",
            SfmlCompletionCatalog.builtIns()
                .get(1)
                .getTemplate());
        assertThrows(
            UnsupportedOperationException.class,
            () -> snapshot.entries()
                .clear());
        assertThrows(
            UnsupportedOperationException.class,
            () -> snapshot.outline()
                .clear());
        assertThrows(
            UnsupportedOperationException.class,
            () -> SfmlCompletionCatalog.builtIns()
                .clear());
    }

    @Test
    void oversizedAndExcessiveSymbolDraftsRemainBounded() {
        StringBuilder many = new StringBuilder();
        for (int i = 0; i < 400; i++) many.append("function f")
            .append(i)
            .append("() returns integer do return 0 end\n");
        Snapshot snapshot = SfmlCompletionCatalog.scan(many.toString());
        assertEquals(SfmlCompletionCatalog.MAX_ENTRIES, players(snapshot).size());
        assertEquals(
            SfmlCompletionCatalog.MAX_ENTRIES,
            snapshot.outline()
                .size());
        assertTrue(snapshot.isTruncated());
        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < SfmlCompletionCatalog.MAX_SOURCE_CHARACTERS + 100; i++) huge.append(' ');
        huge.append("function unseen() returns integer do return 0 end");
        Snapshot bounded = SfmlCompletionCatalog.scan(huge.toString());
        assertTrue(players(bounded).isEmpty());
        assertTrue(bounded.isTruncated());
    }

    private static List<Entry> players(Snapshot snapshot) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : snapshot.entries()) if (entry.getKind() == Kind.PLAYER_FUNCTION) result.add(entry);
        return result;
    }
}
