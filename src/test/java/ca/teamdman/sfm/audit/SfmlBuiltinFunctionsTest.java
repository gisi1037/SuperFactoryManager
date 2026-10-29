package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.FunctionCompiler;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlBuiltinFunctionsTest {

    private static long value(String source) {
        return new DataCompiler().compile(source, 1, 0)
            .evaluate(new DataRuntime(null, 0))
            .asLong();
    }

    @Test
    void checkedIntegerDefaultsHandleBoundsAndCaseWithoutChangingArithmetic() {
        assertEquals(Long.MIN_VALUE, value("mIn(-9223372036854775808, 7)"));
        assertEquals(Long.MAX_VALUE, value("MAX(9223372036854775807, -2)"));
        assertEquals(9, value("ABS(-9)"));
        assertEquals(0, value("ABS(0)"));
        assertEquals(4, value("CLAMP(-5, 4, 8)"));
        assertEquals(6, value("CLAMP(6, 4, 8)"));
        assertEquals(8, value("CLAMP(90, 4, 8)"));
        assertEquals(4, value("CLAMP(90, 4, 4)"));
        assertEquals(Long.MAX_VALUE, value("CLAMP(9223372036854775807, -9223372036854775808, 9223372036854775807)"));
    }

    @Test
    void lengthCountsOuterElementsAndUnicodeCodePointsRatherThanUtf16Units() {
        assertEquals(0, value("LEN(\"\")"));
        assertEquals(3, value("LEN(\"a\\uD83D\\uDE00b\")"));
        assertEquals(2, value("LEN([[1, 2], [3, 4]])"));
        assertEquals(1, value("LEN([{count: 1, name: \"x\"}])"));
        // A known array length cannot skip evaluation of an element that faults.
        assertThrows(IllegalArgumentException.class, () -> value("LEN([1 / 0])"));
    }

    @Test
    void builtinTypeAndArityErrorsAreCompileErrors() {
        for (String source : Arrays.asList(
            "MIN()",
            "MIN(1)",
            "MAX(1, 2, 3)",
            "ABS(1, 2)",
            "CLAMP(1, 2)",
            "MIN(TRUE, 1)",
            "MAX(1, \"2\")",
            "ABS([1])",
            "LEN(1)",
            "LEN({x:1})",
            "LEN(\"a\", \"b\")"))
            assertThrows(IllegalArgumentException.class, () -> new DataCompiler().compile(source, 1, 0), source);
    }

    @Test
    void invalidConstantOperationsKeepConditionalFaultTimingAndCannotPublishStateDefaults() {
        for (String source : Arrays.asList("ABS(-9223372036854775808)", "CLAMP(2, 5, 1)")) {
            DataExpression expression = new DataCompiler().compile(source, 9, 3);
            IllegalArgumentException fault = assertThrows(
                IllegalArgumentException.class,
                () -> expression.evaluate(new DataRuntime(null, 0)));
            assertTrue(
                fault.getMessage()
                    .startsWith("Line 9, Col 3:"),
                fault.getMessage());
            assertThrows(IllegalArgumentException.class, () -> new DataCompiler().declareState("bad", source, 1, 0));
            assertTrue(
                new ProgramBuilder("EVERY 20 TICKS DO IF FALSE THEN LET $x = " + source + " END END").build()
                    .isSuccess());
        }
        DataCompiler defaults = new DataCompiler();
        defaults.declareState("batch", "CLAMP(10, 1, MAX(2, 8))", 1, 0);
        assertEquals(
            8,
            defaults.getStateSchema()
                .getDeclarations()
                .get(0)
                .getDefaultValue()
                .asLong());
    }

    @Test
    void dynamicArgumentsAreReadExactlyOnceFromLeftToRight() {
        List<String> observed = new ArrayList<>();
        DataQueries queries = new DataQueries() {

            @Override
            public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                observed.add(source.getName());
                return source.getName()
                    .equals("value") ? 20
                        : source.getName()
                            .equals("low") ? 5 : 10;
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
        DataExpression expression = new DataCompiler().compile(
            "CLAMP(LABEL(\"value\").count(ITEM(\"*\")), LABEL(\"low\").count(ITEM(\"*\")), LABEL(\"high\").count(ITEM(\"*\")))",
            1,
            0);
        assertEquals(
            10,
            expression.evaluate(new DataRuntime(null, 0, queries))
                .asLong());
        assertEquals(Arrays.asList("value", "low", "high"), observed);
    }

    @Test
    void stringScanAndRepeatedArithmeticUseTheSharedExpressionBudget() {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        compiler.declareLocal("text", DataType.STRING);
        DataExpression expression = compiler.compile("LEN($text)", 1, 0);
        DataRuntime runtime = new DataRuntime(null, 1);
        runtime.write(false, 0, DataValue.string("abcdefgh"));
        runtime.work(16378);
        assertThrows(IllegalArgumentException.class, () -> expression.evaluate(runtime));

        DataExpression math = compiler.compile("MIN(LEN($text), 9)", 1, 0);
        DataRuntime repeated = new DataRuntime(null, 1);
        repeated.write(false, 0, DataValue.string("x"));
        int completed = 0;
        try {
            for (; completed < 16384; completed++) assertEquals(
                1,
                math.evaluate(repeated)
                    .asLong());
            fail("Expected shared expression budget exhaustion");
        } catch (IllegalArgumentException expected) {
            assertTrue(
                expected.getMessage()
                    .contains("budget exhausted"));
        }
        assertTrue(completed > 0 && completed < 16384);
    }

    @Test
    void exactNamePlayerFunctionsStillOverrideDefaultsAndAreNeverConstantFolded() {
        DataCompiler compiler = new DataCompiler();
        SFMLParser parser = new SFMLParser(
            new CommonTokenStream(
                new SFMLLexer(
                    CharStreams
                        .fromString("FUNCTION MIN($a: INTEGER, $b: INTEGER) RETURNS INTEGER DO RETURN $a + $b END"))));
        new FunctionCompiler(compiler).compile(
            parser.program()
                .functionDefinition());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        compiler.beginTrigger();
        compiler.pushScope();
        assertEquals(
            9,
            compiler.compile("MIN(4, 5)", 1, 0)
                .evaluate(new DataRuntime(null, 0))
                .asLong());
        assertEquals(
            4,
            compiler.compile("min(4, 5)", 1, 0)
                .evaluate(new DataRuntime(null, 0))
                .asLong());
        assertThrows(IllegalArgumentException.class, () -> compiler.declareState("bad", "MIN(4,5)", 1, 0));
        assertFalse(
            new ProgramBuilder(
                "STATE $x = MIN(4,5) FUNCTION MIN($a: INTEGER, $b: INTEGER) RETURNS INTEGER DO RETURN $a + $b END")
                    .build()
                    .isSuccess());
    }
}
