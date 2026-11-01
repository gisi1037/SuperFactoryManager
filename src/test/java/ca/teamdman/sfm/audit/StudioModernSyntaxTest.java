package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.StudioLabelRows;
import ca.teamdman.sfm.client.blueprint.StudioSourceMap;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.StudioCompletion;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;

class StudioModernSyntaxTest {

    @Test
    void literalLabelRowsRequireQuotesAndShareStringEscapeValidation() {
        assertThrows(IllegalArgumentException.class, () -> new StudioLabelRows("bare"));
        assertThrows(IllegalArgumentException.class, () -> new StudioLabelRows("label(\"chest\")"));
        assertThrows(IllegalArgumentException.class, () -> new StudioLabelRows("\"\""));
        assertThrows(IllegalArgumentException.class, () -> new StudioLabelRows("\"bad\\q\""));
        assertEquals(
            "\"path\\\\chest\", \"quote\\\"chest\"",
            new StudioLabelRows("\"path\\\\chest\", \"quote\\\"chest\"").source());
    }

    @Test
    void plainBindingsCompleteWithinBraceScopes() {
        String source = "let Shared = 1 every 20 ticks { let Local = 2 print(Lo); }";
        List<StudioCompletion.Candidate> values = StudioCompletion.suggest(source, source.indexOf("Lo);") + 2, 20);
        assertTrue(
            values.stream()
                .anyMatch(
                    value -> value.getText()
                        .equals("Local")));
        String function = "integer choose(integer Argument) { return Arg; }";
        assertTrue(
            StudioCompletion.suggest(function, function.indexOf("Arg;") + 3, 20)
                .stream()
                .anyMatch(
                    value -> value.getText()
                        .equals("Argument")));
    }

    @Test
    void plainAndComputedReceiversOfferQueryMethods() {
        String source = "let inventory = label(\"chest\") every 20 ticks { print(inventory.em); }";
        assertTrue(
            StudioCompletion.suggest(source, source.indexOf("em);") + 2, 20)
                .stream()
                .anyMatch(
                    value -> value.getName()
                        .equals("empty")));
        source = "let index = 1 every 20 ticks { print(label(\"chest\" + index).em); }";
        assertTrue(
            StudioCompletion.suggest(source, source.indexOf("em);") + 2, 20)
                .stream()
                .anyMatch(
                    value -> value.getName()
                        .equals("empty")));
    }

    @Test
    void typeFirstHeadersAppearInInsertAndOutlineWithParameterNames() {
        SfmlCompletionCatalog.Snapshot snapshot = SfmlCompletionCatalog.scan(
            "integer twice(integer count) { return count * 2; } void move(label_ref src, label_ref dest) then end "
                + "every 20 ticks { print(twice(2)); }");
        assertTrue(
            snapshot.entries()
                .stream()
                .anyMatch(
                    entry -> entry.getName()
                        .equals("twice")
                        && entry.getTemplate()
                            .equals("twice(<count>)")
                        && entry.getSignature()
                            .contains("integer")));
        assertTrue(
            snapshot.entries()
                .stream()
                .anyMatch(
                    entry -> entry.getName()
                        .equals("move")
                        && entry.getTemplate()
                            .equals("move(<src>, <dest>)")
                        && entry.getSignature()
                            .endsWith("void")));
    }

    @Test
    void modernTypesBindingsAndReceiverMethodsHaveDistinctColors() {
        String source = "void move(label_ref src) { let contents = src.contents(); }";
        SfmlHighlighter.Line line = SfmlHighlighter.highlight(source).lines.get(0);
        assertEquals(SfmlHighlighter.Style.KEYWORD, line.styleAt(source.indexOf("void")));
        assertEquals(SfmlHighlighter.Style.KEYWORD, line.styleAt(source.indexOf("label_ref")));
        assertEquals(SfmlHighlighter.Style.VARIABLE, line.styleAt(source.lastIndexOf("src")));
        assertEquals(SfmlHighlighter.Style.ACTION, line.styleAt(source.lastIndexOf("contents")));
    }

    @Test
    void basicBraceProgramsImportAndNavigateWithoutConfusingStructBraces() {
        String source = "every 20 ticks { if(true) { print({count: 1}); } else { print(2); } }";
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        assertTrue(
            new ProgramBuilder(BlueprintCompiler.generate(graph)).build()
                .isSuccess());
        StudioSourceMap mapping = StudioSourceMap.build(UUID.randomUUID(), source, source, graph);
        assertNotNull(mapping.atCaret(source.indexOf("print")));
        assertTrue(
            VisualCapabilityAnalyzer.analyze(new ProgramBuilder(source).buildWithSyntax())
                .isAvailable());
    }

    @Test
    void plainVariablePredicatesStayTextOnlyRatherThanExposingAnIncompleteGraph() {
        String source = "let ready = true every 20 ticks { if(ready) { print(1); } }";
        assertFalse(
            VisualCapabilityAnalyzer.analyze(new ProgramBuilder(source).buildWithSyntax())
                .isAvailable());
    }
}
