package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Blocker;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Kind;

class VisualCapabilityTest {

    @Test
    void supportedSyntaxIsAvailableWithoutAnyProfileInput() {
        String source = "NAME \"Keep Case\"\n-- explanation\nEVERY 20 GLOBAL PLUS 2 TICKS DO\n"
            + "INPUT 64 EACH RETAIN 8 EACH minecraft:iron_ingot FROM EACH source ROUND ROBIN BY BLOCK TOP SIDE SLOTS 0-8\n"
            + "IF NOT (SOME source HAS >= 3 minecraft:iron_ingot) THEN OUTPUT 4 TO dest ELSE FORGET END\nEND";
        BlueprintCompiler.ImportResult imported = BlueprintCompiler.inspectText(source);
        assertTrue(imported.report.isAvailable(), imported.report.getMessage());
        assertNotNull(imported.graph);
        String generated = BlueprintCompiler.generate(imported.graph);
        assertTrue(
            new ProgramBuilder(generated).build()
                .isSuccess());
        assertTrue(generated.contains("64 EACH RETAIN 8 EACH"));
        assertTrue(BlueprintCompiler.inspectText(generated).report.isAvailable());
        assertTrue(BlueprintCompiler.inspectText("").report.isAvailable());
    }

    @Test
    void reportsAllDataConstructsIncludingUnreachableNestedStatements() {
        String source = "STATE $count = 1\nEVERY 20 TICKS DO\n"
            + " LET $amount = 3\n IF FALSE THEN\n  SET $count = $count + 1\n"
            + "  INPUT ($amount) FROM source\n END\n IF ($count > 0) THEN FORGET END\nEND";
        BlueprintCompiler.ImportResult result = BlueprintCompiler.inspectText(source);
        assertNull(result.graph);
        assertEquals(
            Arrays.asList(
                Kind.STATE_DECLARATION,
                Kind.LOCAL_DECLARATION,
                Kind.ASSIGNMENT,
                Kind.DATA_EXPRESSION,
                Kind.DATA_EXPRESSION),
            kinds(result.report));
        assertEquals(
            Arrays.asList(1, 3, 5, 6, 8),
            result.report.getBlockers()
                .stream()
                .map(blocker -> blocker.line)
                .collect(Collectors.toList()));
        Blocker input = result.report.getBlockers()
            .get(3);
        assertEquals("$amount", codePointSlice(source, input));
        BlueprintCompiler.ImportException exception = assertThrows(
            BlueprintCompiler.ImportException.class,
            () -> BlueprintCompiler.importText(source));
        assertEquals(kinds(result.report), kinds(exception.getReport()));
        assertTrue(BlueprintCompiler.inspectText("EVERY 20 TICKS DO INPUT 3 FROM source END").report.isAvailable());
    }

    @Test
    void elseIfAndNestedDataAreReportedTogetherAtTheirActualLocations() {
        String source = "EVERY 20 TICKS DO\n IF FALSE THEN\n" + "  IF TRUE THEN FORGET ELSE IF FALSE THEN FORGET END\n"
            + " ELSE IF (1 > 0) THEN FORGET ELSE IF FALSE THEN FORGET END\nEND";
        VisualCapabilityReport report = BlueprintCompiler.inspectText(source).report;
        assertEquals(
            3,
            report.getBlockers()
                .stream()
                .filter(b -> b.kind == Kind.ELSE_IF)
                .count());
        assertEquals(
            1,
            report.getBlockers()
                .stream()
                .filter(b -> b.kind == Kind.DATA_EXPRESSION)
                .count());
        for (Blocker blocker : report.getBlockers()) {
            assertTrue(blocker.line >= 3);
            if (blocker.kind == Kind.ELSE_IF) assertTrue(codePointSlice(source, blocker).startsWith("ELSE IF"));
        }
    }

    @Test
    void malformedAndSemanticallyInvalidSourceNeverExposePartialGraphs() {
        for (String source : Arrays.asList(
            "EVERY nope",
            "EVERY 20 TICKS DO\nINPUT FROM\nEND",
            "EVERY 20 TICKS DO INPUT ($undefined) FROM source END")) {
            BlueprintCompiler.ImportResult result = BlueprintCompiler.inspectText(source);
            assertNull(result.graph);
            assertFalse(result.report.isAvailable());
            assertTrue(
                result.report.getBlockers()
                    .stream()
                    .allMatch(b -> b.kind == Kind.INVALID_SOURCE));
            assertTrue(
                result.report.getBlockers()
                    .get(0).line > 0,
                result.report.getMessage());
        }
    }

    @Test
    void reportIsImmutableAndOrdinaryBuildDoesNotRetainSyntax() {
        ProgramBuildResult ordinary = new ProgramBuilder("EVERY 20 TICKS DO END").build();
        assertTrue(ordinary.isSuccess());
        assertNull(ordinary.getSyntax());
        assertNull(ordinary.getTokens());
        ProgramBuildResult parsed = new ProgramBuilder("EVERY 20 TICKS DO LET $n = 2 END").buildWithSyntax();
        VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(parsed);
        assertEquals(Arrays.asList(Kind.LOCAL_DECLARATION), kinds(report));
        assertThrows(
            UnsupportedOperationException.class,
            () -> report.getBlockers()
                .clear());
        List<Blocker> original = new ArrayList<>(report.getBlockers());
        VisualCapabilityReport copy = new VisualCapabilityReport(original);
        original.clear();
        assertEquals(
            1,
            copy.getBlockers()
                .size());
    }

    @Test
    void graphLimitsStillBlockVisualAndDataCannotBeInjectedThroughAClause() {
        StringBuilder source = new StringBuilder("EVERY 20 TICKS DO\n");
        for (int i = 0; i < 256; i++) source.append("FORGET\n");
        source.append("END");
        BlueprintCompiler.ImportResult result = BlueprintCompiler.inspectText(source.toString());
        assertNull(result.graph);
        assertEquals(
            Kind.GRAPH_LIMIT,
            result.report.getBlockers()
                .get(0).kind);
        assertEquals(
            257,
            result.report.getBlockers()
                .get(0).line);
        assertThrows(
            BlueprintCompiler.ImportException.class,
            () -> BlueprintCompiler.validateClause(BlueprintGraph.Type.INPUT, "INPUT (1 + 2) FROM source"));
    }

    @Test
    void locationsAccountForUnicodeBeforeTheUnsupportedConstruct() {
        String source = "-- 😀 explanation\nEVERY 20 TICKS DO LET $n = 2 END";
        Blocker blocker = BlueprintCompiler.inspectText(source).report.getBlockers()
            .get(0);
        assertEquals(2, blocker.line);
        assertEquals(18, blocker.column);
        assertEquals("LET $n = 2", codePointSlice(source, blocker));
    }

    private static List<Kind> kinds(VisualCapabilityReport report) {
        return report.getBlockers()
            .stream()
            .map(blocker -> blocker.kind)
            .collect(Collectors.toList());
    }

    private static String codePointSlice(String source, Blocker blocker) {
        return source.substring(
            source.offsetByCodePoints(0, blocker.startOffset),
            source.offsetByCodePoints(0, blocker.endOffset));
    }
}
