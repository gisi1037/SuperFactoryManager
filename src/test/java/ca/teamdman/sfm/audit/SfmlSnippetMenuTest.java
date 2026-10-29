package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import ca.teamdman.sfm.client.gui.mui2.Mui2CodeEditor.HeaderLayout;
import ca.teamdman.sfm.client.gui.syntax.SnippetCategory;
import ca.teamdman.sfm.client.gui.syntax.SyntaxRegistry;
import ca.teamdman.sfm.client.gui.syntax.SyntaxSnippet;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Palette examples are fragments; these explicit replacements and wrappers document their insertion contexts. */
class SfmlSnippetMenuTest {

    @TestFactory
    List<DynamicTest> everyPaletteEntryCompilesInItsDocumentedContext() {
        List<DynamicTest> tests = new ArrayList<>();
        for (SyntaxSnippet snippet : SyntaxRegistry.getAllSnippets()) {
            tests.add(DynamicTest.dynamicTest(snippet.getId() + ": " + snippet.getTitle(), () -> {
                String source = completeProgram(snippet);
                ProgramBuildResult result = new ProgramBuilder(source).build();
                assertTrue(result.isSuccess(), result.getErrorMessage() + "\n" + source);
            }));
        }
        return tests;
    }

    @Test
    void everyFeatureCategoryIsDiscoverableWithUniqueEntriesAndDescriptions() {
        Set<String> ids = new HashSet<>();
        for (SnippetCategory category : SnippetCategory.values()) {
            assertFalse(
                SyntaxRegistry.getSnippetsForCategory(category)
                    .isEmpty(),
                category.name());
            assertFalse(
                category.getDescription()
                    .trim()
                    .isEmpty());
            for (SyntaxSnippet snippet : SyntaxRegistry.getSnippetsForCategory(category)) {
                assertTrue(ids.add(snippet.getId()), snippet.getId());
                assertFalse(
                    snippet.getDescription()
                        .trim()
                        .isEmpty(),
                    snippet.getId());
                assertFalse(
                    snippet.getTemplate()
                        .trim()
                        .isEmpty(),
                    snippet.getId());
                assertEquals(category, snippet.getCategory());
            }
        }
        assertEquals(
            SyntaxRegistry.getAllSnippets()
                .size(),
            ids.size());
        for (String id : Arrays.asList(
            "data_let",
            "data_global_let",
            "data_logic_aliases",
            "data_nbt_compare",
            "data_state",
            "data_set",
            "loop_counted_for",
            "loop_array_for",
            "loop_while",
            "array_set",
            "struct_nested",
            "string_concat",
            "string_equal",
            "string_escapes",
            "math_arithmetic",
            "math_bitwise",
            "math_shift",
            "query_ore",
            "query_contents",
            "query_contents_filtered",
            "query_contents_loop",
            "query_stock_helpers",
            "data_min_max",
            "data_clamp",
            "data_abs",
            "data_len",
            "query_side",
            "query_slots",
            "query_empty_all",
            "query_available_all",
            "trig_port_rising",
            "trig_port_pulse",
            "trig_port_falling",
            "trig_port_gate",
            "io_transfer_values",
            "io_transfer_exact",
            "io_redstone_dynamic",
            "io_redstone_forward",
            "query_redstone",
            "query_powered",
            "query_redstone_available",
            "function_redstone_query",
            "cond_typed_count")) assertTrue(ids.contains(id), id);
    }

    @Test
    void shiftAndComparisonOperatorsCannotSwallowPlaceholders() {
        String source = "LET $bits = 1 << 3\nIF ($bits < 16 AND $bits > 0) THEN\n"
            + "INPUT <quantity> FROM <src>\nEND\nLET $x = $bits << 2 > 0\nSLOTS <first>-<last>";
        SyntaxSnippet snippet = new SyntaxSnippet("operators", "Operators", SnippetCategory.MATH, source, "Fixture");
        List<String> expected = Arrays.asList("<quantity>", "<src>", "<first>", "<last>");
        assertEquals(expected, snippet.getPlaceholders());
        List<String> navigation = new ArrayList<>();
        Matcher matcher = SyntaxSnippet.PLACEHOLDER_PATTERN.matcher(source);
        while (matcher.find()) navigation.add(matcher.group());
        assertEquals(expected, navigation, "Insertion metadata and editor Tab navigation share the same matcher");
        assertEquals(
            Collections.singletonList("<20>"),
            new SyntaxSnippet("numeric", "Timer", SnippetCategory.TRIGGER, "EVERY <20> TICKS DO END", "Fixture")
                .getPlaceholders());
    }

    @Test
    void headerRowsFitMinimumAndLargerLogicalScreenWidths() {
        int count = SnippetCategory.values().length;
        for (int screenWidth : new int[] { 320, 360, 480, 640, 1280 }) {
            int panelWidth = (int) (screenWidth * .98f);
            HeaderLayout layout = new HeaderLayout(panelWidth, count);
            assertTrue(layout.buttonWidth >= 52, "Readable buttons at " + screenWidth);
            for (int i = 0; i < count; i++) {
                assertTrue(layout.x(i) >= 8);
                assertTrue(layout.x(i) + layout.buttonWidth <= panelWidth - 8);
                assertTrue(layout.y(i) >= 47, "Snippet menus stay below the six editing controls");
                assertTrue(layout.y(i) + 20 < layout.editorTop, "Menus stay above source text");
                for (int j = i + 1; j < count; j++) assertTrue(
                    layout.y(i) != layout.y(j) || layout.x(i) + layout.buttonWidth < layout.x(j)
                        || layout.x(j) + layout.buttonWidth < layout.x(i),
                    "Category buttons must not overlap");
            }
            if (screenWidth == 320)
                assertTrue((int) (240 * .98f) - layout.editorTop - 61 >= 60, "At least six visible code lines");
        }
        assertEquals(5, new HeaderLayout(313, count).columns);
        assertEquals(10, new HeaderLayout(640, count).columns);
    }

    @Test
    void suggestionReplacementRecognizesOnlyThePlaceholderUnderTheCaret() {
        String operators = "IF (($n << 2) > 0 AND $n < 64 AND $n > 0) THEN\nINPUT <quantity> FROM <src>";
        for (int caret = 0; caret < operators.indexOf('\n'); caret++)
            assertNull(SyntaxSnippet.placeholderAt(operators, caret), "No expression may be replaced at " + caret);
        int start = operators.indexOf("<quantity>");
        int end = start + "<quantity>".length();
        for (int caret = start; caret <= end; caret++)
            assertArrayEquals(new int[] { start, end }, SyntaxSnippet.placeholderAt(operators, caret));
        int[] selected = SyntaxSnippet.placeholderAt(operators, end);
        assertEquals(
            "IF (($n << 2) > 0 AND $n < 64 AND $n > 0) THEN\nINPUT 8 FROM <src>",
            operators.substring(0, selected[0]) + "8" + operators.substring(selected[1]),
            "Replacing a Tab-selected placeholder must preserve adjacent operators and the next placeholder");
        assertNull(SyntaxSnippet.placeholderAt("FOR $i = 0 TO 3 STEP 1 DO", 8));
    }

    @Test
    void dataAndIfSuggestionsExposeNewImplementedFeatures() {
        for (String source : new String[] { "EVERY 20 TICKS DO\nLET $", "EVERY 20 TICKS DO\nIF " }) {
            Set<SnippetCategory> found = new HashSet<>();
            for (SyntaxSnippet snippet : SyntaxRegistry.getContextualSuggestions(
                source,
                1,
                source.substring(source.indexOf('\n') + 1)
                    .length(),
                Collections.emptySet())) found.add(snippet.getCategory());
            assertTrue(found.contains(SnippetCategory.QUERY));
            assertTrue(found.contains(SnippetCategory.MATH));
            assertTrue(found.contains(SnippetCategory.STRING));
        }
    }

    private static String completeProgram(SyntaxSnippet snippet) {
        String fragment = replacePlaceholders(snippet);
        String id = snippet.getId();
        if (id.equals("data_global_let") || id.equals("data_nbt_compare")) return fragment;
        if (id.startsWith("function_")) return fragment;
        if (fragment.startsWith("NAME ")) return fragment + "\nEVERY 20 TICKS DO END";
        if (fragment.startsWith("STATE "))
            return fragment.contains("EVERY ") ? fragment : fragment + "\nEVERY 20 TICKS DO END";
        if (snippet.getCategory() == SnippetCategory.TRIGGER) return fragment;

        if (id.startsWith("cond_") && !fragment.startsWith("IF ")) fragment = "IF " + fragment + " THEN END";
        else if (snippet.getCategory() == SnippetCategory.LABEL && !fragment.startsWith("LET "))
            fragment = "INPUT FROM " + fragment;
        else if (snippet.getCategory() == SnippetCategory.MODIFIER) {
            if (id.startsWith("mod_slot")) fragment = "INPUT FROM source " + fragment;
            else if (id.startsWith("mod_round")) fragment = "OUTPUT TO dest " + fragment;
            else fragment = "INPUT " + fragment + " FROM source";
        }
        if (id.equals("data_set")) fragment = "LET $value = 0\n" + fragment;
        return "EVERY 20 TICKS DO\n" + fragment + "\nEND";
    }

    private static String replacePlaceholders(SyntaxSnippet snippet) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("<20>", "20");
        replacements.put("<15>", "15");
        replacements.put("<0>", "0");
        replacements.put("<seconds>", "1");
        replacements.put("<offset>", "0");
        replacements.put("<start>", "0");
        replacements.put("<end>", "3");
        replacements.put("<step>", "1");
        replacements.put("<array>", "[1, 2, 3]");
        replacements.put("<statements>", "FORGET");
        replacements.put("<fallback>", "FORGET");
        replacements.put(
            "<condition>",
            snippet.getId()
                .equals("loop_while") ? "FALSE" : "TRUE");
        replacements.put("<other_condition>", "FALSE");
        replacements.put("<src>", "source");
        replacements.put("<dst>", "dest");
        replacements.put("<label>", "source");
        replacements.put("<emit>", "emitter");
        replacements.put("<sensor>", "sensor");
        replacements.put("<quantity>", "8");
        replacements.put("<amount>", "4");
        replacements.put("<minimum>", "0");
        replacements.put("<maximum>", "8");
        replacements.put("<level>", "1");
        replacements.put("<item>", "minecraft:iron_ingot");
        replacements.put("<ore_tag>", "ingotIron");
        replacements.put("<first>", "0");
        replacements.put("<last>", "8");
        replacements.put("<name>", "value");
        replacements.put("<value>", "8");
        replacements.put("<text>", "ready");
        String result = snippet.getTemplate();
        for (String placeholder : snippet.getPlaceholders()) {
            assertTrue(replacements.containsKey(placeholder), "Document a fixture replacement for " + placeholder);
            result = result.replace(placeholder, replacements.get(placeholder));
        }
        assertFalse(
            SyntaxSnippet.PLACEHOLDER_PATTERN.matcher(result)
                .find(),
            snippet.getId());
        return result;
    }
}
