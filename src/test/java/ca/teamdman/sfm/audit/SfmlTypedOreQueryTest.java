package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.DebugValueFormatter;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlTypedOreQueryTest {

    @Test
    void oreConstructorReusesSelectorTypeAndRemainsLocalInNestedCollections() {
        DataCompiler compiler = new DataCompiler();
        DataValue value = compiler.compile("[{what: oRe(\"ingotIron\"), source: LABEL(\"stock\")}]", 1, 0)
            .evaluate(new DataRuntime(null, 0));
        assertEquals(
            DataType.RESOURCE_SELECTOR,
            value.get(0)
                .get("what")
                .getType());
        assertEquals(
            "ORE(\"ingotIron\")",
            DebugValueFormatter.format(
                value.get(0)
                    .get("what")));
        assertEquals(Collections.singleton("stock"), compiler.getReferencedLabels());
        assertThrows(IllegalArgumentException.class, () -> compiler.declareState("bad", "ORE(\"ingotIron\")", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> compiler.declareState("bad", "[ORE(\"ingotIron\")]", 1, 0));
    }

    @Test
    void oreSelectorsPassThroughExistingTypedLabelQueryBoundary() {
        int[] calls = { 0 };
        DataQueries queries = new DataQueries() {

            @Override
            public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                calls[0]++;
                assertEquals("stock", source.getName());
                assertEquals("ORE(\"ingotIron\")", selector.toString());
                return 12;
            }

            @Override
            public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                throw new AssertionError("Unexpected query");
            }

            @Override
            public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                throw new AssertionError("Unexpected query");
            }
        };
        assertEquals(
            8,
            new DataCompiler().compile("MIN(LABEL(\"stock\").count(ORE(\"ingotIron\")), 8)", 1, 0)
                .evaluate(new DataRuntime(null, 0, queries))
                .asLong());
        assertEquals(1, calls[0]);
    }

    @Test
    void constructorsRejectExpressionsAndPlayerNameCollisionsWithoutBroadeningSelectors() {
        for (String expression : new String[] { "ORE()", "ORE(1)", "ORE(\"ingot\" + \"Iron\")",
            "ORE(\"ingotIron\", \"dustIron\")", "ORE(\"*\")" })
            assertThrows(
                IllegalArgumentException.class,
                () -> new DataCompiler().compile(expression, 1, 0),
                expression);
        ProgramBuildResult invalid = new ProgramBuilder("FUNCTION ORE($x: INTEGER) RETURNS INTEGER DO RETURN $x END")
            .build();
        assertFalse(invalid.isSuccess());
        assertTrue(
            invalid.getErrorMessage()
                .contains("Reserved built-in function"),
            invalid.getErrorMessage());
        ProgramBuildResult valid = new ProgramBuilder(
            "FUNCTION iron($source: LABEL_REF) RETURNS INTEGER DO RETURN $source.count(ORE(\"ingotIron\")) END EVERY 20 TICKS DO LET $n = iron(LABEL(\"stock\")) END")
                .build();
        assertTrue(valid.isSuccess(), valid.getErrorMessage());
        assertTrue(
            new ProgramBuilder(
                valid.getProgram()
                    .toString()).build()
                        .isSuccess());
    }
}
