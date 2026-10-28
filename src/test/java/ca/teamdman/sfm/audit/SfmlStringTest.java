package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ExpressionLimits;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlStringTest {

    private static String repeat(char character, int count) {
        char[] characters = new char[count];
        Arrays.fill(characters, character);
        return new String(characters);
    }

    private static DataValue evaluate(String source) {
        return new DataCompiler().compile(source, 1, 0)
            .evaluate(new DataRuntime(null, 0));
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static void tick(Program program, MemoryState state) {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setStateAccess(state);
        program.tick(context);
    }

    @Test
    void literalsDecodeEscapesUnicodeAndPreserveQuotedComments() {
        String expected = "\"\\\n\r\t\b\f\u0000\u00e9\u4e2d\ud83d\ude00 -- literal";
        String literal = "\"\\\"\\\\\\n\\r\\t\\b\\f\\u0000\\u00e9\\u4e2d\\uD83D\\uDE00 -- literal\"";
        assertEquals(expected, evaluate(literal).asString());
        assertEquals("", evaluate("\"\"").asString());
        assertEquals("line one\nline two", evaluate("\"line one\nline two\"").asString());
        assertEquals("A -- inside B", evaluate("\"A -- inside\" -- outside\r + \" B\"").asString());
        assertEquals("A\\B", evaluate("\"A\\\\\" + \"B\"").asString());
        assertEquals(
            expected,
            evaluate(
                DataValue.string(expected)
                    .toString()).asString());
        assertEquals("\ud83d\ude00", evaluate("\"\ud83d\ude00\"").asString());
    }

    @Test
    void invalidEscapesAndUnpairedSurrogatesAreCompileErrors() {
        String[] invalid = { "\"\\q\"", "\"\\u123\"", "\"\\uZZZZ\"", "\"\\uD800\"", "\"\\uDC00\"", "\"\\uD800x\"",
            "\"unterminated", "\"trailing\\\"", "\"" + '\ud800' + "\"" };
        for (String source : invalid) assertThrows(IllegalArgumentException.class, () -> evaluate(source), source);
        assertThrows(IllegalArgumentException.class, () -> DataValue.string("\ud800"));
        assertThrows(IllegalArgumentException.class, () -> DataValue.string("\udc00"));
        assertThrows(IllegalArgumentException.class, () -> DataValue.string(null));
        assertThrows(
            IllegalArgumentException.class,
            () -> DataValue.integer(1)
                .asString());
    }

    @Test
    void stringsHaveFixedScalarTypesWithoutCoercionOrderingOrIndexing() {
        String[] invalid = { "\"1\" + 1", "1 + \"1\"", "\"a\" - \"b\"", "\"a\" * \"b\"", "\"a\" < \"b\"",
            "\"a\" = TRUE", "\"a\" AND TRUE", "-\"a\"", "\"a\"[0]", "\"a\".length", "[\"a\", 1]", "[\"a\"] = [\"a\"]" };
        for (String source : invalid) assertThrows(IllegalArgumentException.class, () -> evaluate(source), source);
        assertSame(DataType.STRING, evaluate("\"a\" + \"bc\"").getType());
        assertTrue(evaluate("\"a\" EQ \"a\"").asBoolean());
        assertFalse(evaluate("\"A\" = \"a\"").asBoolean());
        assertTrue(evaluate("NOT (\"a\" = \"b\")").asBoolean());
        assertEquals(DataType.array(DataType.STRING, 2), evaluate("[\"\", \"longer\"]").getType());
        assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO LET $s = \"\" SET $s = 1 END").build()
                .isSuccess());
    }

    @Test
    void nestedStringUpdatesPreserveAliasesAndUseDotMemberAccess() {
        Program program = program(
            "STATE $out = {before: \"\", after: \"\", same: FALSE} EVERY 20 TICKS DO "
                + "LET $items = [{id: \"minecraft:stone\", count: 3}, {id: \"minecraft:dirt\", count: 7}] "
                + "LET $copy = $items SET $copy[0].id = $items[1].id + \"_variant\" "
                + "SET $out.before = $items[0].id SET $out.after = $copy[0].id "
                + "SET $out.same = NOT ($items[0].id = $copy[0].id) END");
        MemoryState state = new MemoryState(program.getStateSchema());
        tick(program, state);
        assertEquals(
            "minecraft:stone",
            state.read(0)
                .get("before")
                .asString());
        assertEquals(
            "minecraft:dirt_variant",
            state.read(0)
                .get("after")
                .asString());
        assertTrue(
            state.read(0)
                .get("same")
                .asBoolean());
    }

    @Test
    void programCanonicalRoundTripKeepsTrailingBackslashesEscapesAndMultilineStrings() {
        String text = "a\\\" -- text\n\ud83d\ude00\\";
        Program original = program(
            "STATE $out = " + DataValue.string(text)
                + " EVERY 20 TICKS DO "
                + "LET $suffix = \"\\\\\" + \"end\" SET $out = $out + $suffix END");
        Program rebuilt = program(original.toString());
        MemoryState state = new MemoryState(rebuilt.getStateSchema());
        tick(rebuilt, state);
        assertEquals(
            text + "\\end",
            state.read(0)
                .asString());
        Program multiline = program("STATE $out = \"first\nsecond -- literal\" EVERY 20 TICKS DO END");
        assertEquals(
            "first\nsecond -- literal",
            multiline.getStateSchema()
                .getDeclarations()
                .get(0)
                .getDefaultValue()
                .asString());
    }

    @Test
    void cachedAggregatePayloadMatchesModifiedUtfAndTracksReplacement() throws Exception {
        DataValue value = DataValue.string("A\u0000\u00e9\u4e2d\ud83d\ude00");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new DataOutputStream(bytes).writeUTF(value.asString());
        assertEquals(bytes.size() - 2, value.getStringNbtBytes());
        assertEquals(6, value.getStringCharacterCount());
        DataValue array = DataValue.array(Arrays.asList(value, value));
        DataValue nested = DataValue.array(Arrays.asList(array, array));
        assertEquals(24, nested.getStringCharacterCount());
        assertEquals(4 * value.getStringNbtBytes(), nested.getStringNbtBytes());
        DataValue replacement = array.with(0, DataValue.string("x"));
        assertEquals(7, replacement.getStringCharacterCount());
        assertEquals(1 + value.getStringNbtBytes(), replacement.getStringNbtBytes());
        assertEquals(12, array.getStringCharacterCount());
        assertSame(value, replacement.get(1));
        assertEquals(
            0,
            DataValue.integer(9)
                .getStringCharacterCount());
        assertEquals(
            0,
            DataValue.bool(true)
                .getStringNbtBytes());
    }

    @Test
    void literalAndAggregateConstantBudgetsIncludeDynamicExpressions() {
        String maximum = repeat('x', ExpressionLimits.MAX_STRING_LENGTH);
        assertEquals(maximum, evaluate("\"" + maximum + "\"").asString());
        assertThrows(IllegalArgumentException.class, () -> evaluate("\"" + maximum + "x\""));
        assertThrows(IllegalArgumentException.class, () -> DataValue.string(maximum + "x"));
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        compiler.declareLocal("s", DataType.STRING);
        for (int i = 0; i < 4; i++) compiler.compile("$s + \"" + maximum + "\"", 1, 0);
        IllegalArgumentException fault = assertThrows(
            IllegalArgumentException.class,
            () -> compiler.compile("$s + \"x\"", 1, 0));
        assertTrue(
            fault.getMessage()
                .contains("Constant pool"));
        List<ProgramStateSchema.Declaration> declarations = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            declarations.add(new ProgramStateSchema.Declaration("s" + i, DataValue.string(maximum)));
        assertEquals(4, new ProgramStateSchema(declarations).size());
        declarations.add(new ProgramStateSchema.Declaration("extra", DataValue.string("x")));
        assertThrows(IllegalArgumentException.class, () -> new ProgramStateSchema(declarations));
    }

    @Test
    void immutableReadsAndFoldedConstantsReusePayloadWithoutRuntimeAllocation() {
        DataExpression folded = new DataCompiler().compile("\"left\" + \"right\"", 1, 0);
        DataRuntime runtime = new DataRuntime(null, 0);
        DataValue first = folded.evaluate(runtime);
        assertSame(first, folded.evaluate(runtime));
        assertEquals("leftright", first.asString());
        assertEquals(0, runtime.getAllocatedStringCharacters());
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        DataCompiler.Binding binding = compiler.declareLocal("s", DataType.STRING);
        DataRuntime locals = new DataRuntime(null, 1);
        binding.write(locals, first);
        assertSame(
            first,
            compiler.compile("$s", 1, 0)
                .evaluate(locals));
        assertEquals(0, locals.getAllocatedStringCharacters());
    }

    @Test
    void concatenationAndEqualityChargeCharacterWorkAndAllocation() {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        DataCompiler.Binding a = compiler.declareLocal("a", DataType.STRING);
        DataCompiler.Binding b = compiler.declareLocal("b", DataType.STRING);
        DataRuntime runtime = new DataRuntime(null, 2);
        a.write(runtime, DataValue.string(new String(new char[] { 'a', 'b', 'c' })));
        b.write(runtime, DataValue.string(new String(new char[] { 'a', 'b', 'c' })));
        int before = runtime.getWork();
        assertTrue(
            compiler.compile("$a = $b", 1, 0)
                .evaluate(runtime)
                .asBoolean());
        assertEquals(6, runtime.getWork() - before);
        before = runtime.getWork();
        assertEquals(
            "abcabc",
            compiler.compile("$a + $b", 1, 0)
                .evaluate(runtime)
                .asString());
        assertEquals(9, runtime.getWork() - before);
        assertEquals(6, runtime.getAllocatedStringCharacters());
    }

    @Test
    void failedStringAssignmentsPublishNothingAndShortCircuitSkipsFaults() {
        String maximum = repeat('x', ExpressionLimits.MAX_STRING_LENGTH);
        Program program = program(
            "STATE $text = \"" + maximum
                + "\" STATE $marker = 0 EVERY 20 TICKS DO "
                + "SET $marker = 7 IF (FALSE AND $text + \"!\" = \"\") THEN SET $marker = 99 END "
                + "IF (TRUE OR $text + \"!\" = \"\") THEN SET $marker = 8 END "
                + "SET $text = $text + \"!\" SET $marker = 9 END");
        MemoryState state = new MemoryState(program.getStateSchema());
        assertThrows(IllegalArgumentException.class, () -> tick(program, state));
        assertEquals(
            maximum,
            state.read(0)
                .asString());
        assertEquals(
            8,
            state.read(1)
                .asLong());
        assertFalse(
            new ProgramBuilder("STATE $text = \"" + maximum + "\" + \"!\" EVERY 20 TICKS DO END").build()
                .isSuccess());
    }

    @Test
    void allocationAndWorkFaultsHappenBeforeStatePublication() {
        DataCompiler compiler = new DataCompiler();
        compiler.declareState("text", "\"original\"", 1, 0);
        MemoryState state = new MemoryState(compiler.getStateSchema());
        DataValue original = state.read(0);
        DataExpression rhs = compiler.compile("$text + \"!\"", 1, 0);
        DataCompiler.Assignment assignment = compiler.assignment("$text", rhs, 1, 0);
        DataRuntime exhaustedAllocation = new DataRuntime(state, 0);
        exhaustedAllocation.allocateString(ExpressionLimits.MAX_ALLOCATED_STRING_CHARACTERS);
        assertThrows(IllegalArgumentException.class, () -> assignment.execute(exhaustedAllocation));
        assertSame(original, state.read(0));
        DataRuntime exhaustedWork = new DataRuntime(state, 0);
        exhaustedWork.work(ExpressionLimits.MAX_WORK - 5);
        assertThrows(IllegalArgumentException.class, () -> assignment.execute(exhaustedWork));
        assertSame(original, state.read(0));
        assertEquals(0, exhaustedWork.getAllocatedStringCharacters());
    }

    private static final class MemoryState implements StateAccess {

        private final DataValue[] values;

        MemoryState(ProgramStateSchema schema) {
            values = new DataValue[schema.size()];
            for (int i = 0; i < values.length; i++) values[i] = schema.getDeclarations()
                .get(i)
                .getDefaultValue();
        }

        @Override
        public DataValue read(int slot) {
            return values[slot];
        }

        @Override
        public void write(int slot, DataValue value) {
            assertEquals(values[slot].getType(), value.getType());
            values[slot] = value;
        }
    }
}
