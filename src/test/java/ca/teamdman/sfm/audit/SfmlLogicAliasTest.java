package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.ComparisonOperator;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlLogicAliasTest {

    @Test
    void symbolicLogicMatchesKeywordPrecedenceAndScalarEqualityRules() {
        Object[][] cases = { { "!FALSE", true }, { "!!TRUE", true }, { "TRUE&&FALSE", false },
            { "FALSE||TRUE&&FALSE", false }, { "TRUE||FALSE&&FALSE", true }, { "!1 == 2", true },
            { "!(1 == 1)", false }, { "1!=2", true }, { "1!=1", false },
            { "-9223372036854775808!=9223372036854775807", true }, { "TRUE!=FALSE", true }, { "FALSE!=FALSE", false },
            { "TRUE==TRUE", true }, { "\"same\"!=\"same\"", false }, { "\"same\"!=\"different\"", true },
            { "\"\"==\"\"", true }, { "6 & 3 == 2 && 4 | 1 != 4 || FALSE", true },
            { "NOT FALSE AND 2 EQ 2 OR FALSE", true } };
        for (Object[] test : cases) {
            String expression = (String) test[0];
            boolean expected = (Boolean) test[1];
            assertEquals(expected, evaluate(expression, null).asBoolean(), expression);
            Program program = program("STATE $r = " + expression + " EVERY 20 TICKS DO END");
            assertEquals(
                expected,
                program.getStateSchema()
                    .getDeclarations()
                    .get(0)
                    .getDefaultValue()
                    .asBoolean(),
                expression);
        }
    }

    @Test
    void dynamicScalarsUseTheSameAliasesWithoutCoercion() {
        for (String expression : Arrays
            .asList("$n!=8 && $flag==TRUE && $text!=\"other\"", "!($n==8) AND $flag OR FALSE")) {
            Program program = program(
                "STATE $r = FALSE EVERY 20 TICKS DO LET $n = 7 LET $flag = TRUE LET $text = \"same\" " + "SET $r = "
                    + expression
                    + " END");
            Memory memory = new Memory(program);
            ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
            context.setStateAccess(memory);
            program.tick(context);
            assertTrue(memory.value.asBoolean(), expression);
        }
    }

    @Test
    void aliasesShortCircuitExactlyLikeAndOrAndEvaluateComparisonsLeftToRight() {
        RecordingQueries queries = new RecordingQueries();
        String right = "LABEL(\"right\").count(ITEM(\"*\")) != 0";
        assertFalse(evaluate("FALSE && " + right, queries).asBoolean());
        assertTrue(evaluate("TRUE || " + right, queries).asBoolean());
        assertTrue(evaluate("TRUE || FALSE && " + right, queries).asBoolean());
        assertTrue(queries.calls.isEmpty());
        assertTrue(evaluate("TRUE && " + right, queries).asBoolean());
        assertEquals(Arrays.asList("right"), queries.calls);
        queries.calls.clear();
        assertTrue(evaluate("FALSE || " + right, queries).asBoolean());
        assertEquals(Arrays.asList("right"), queries.calls);
        queries.calls.clear();
        assertTrue(
            evaluate("LABEL(\"left\").count(ITEM(\"*\"))!=" + "LABEL(\"right\").count(ITEM(\"*\"))", queries)
                .asBoolean());
        assertEquals(Arrays.asList("left", "right"), queries.calls);
    }

    @Test
    void inequalityForItemKeysUsesRegistryMetadataAndFullNbt() {
        ItemIdentity original = key("alias:item", 7, tag("same"));
        assertFalse(compareKeys("!=", original, key("alias:item", 7, tag("same"))));
        assertTrue(compareKeys("==", original, key("alias:item", 7, tag("same"))));
        assertTrue(compareKeys("!=", original, key("alias:other", 7, tag("same"))));
        assertTrue(compareKeys("!=", original, key("alias:item", 8, tag("same"))));
        assertTrue(compareKeys("!=", original, key("alias:item", 7, tag("different"))));
        assertTrue(compareKeys("!=", key("alias:item", 7, null), key("alias:item", 7, new NBTTagCompound())));
    }

    @Test
    void inequalityCannotBypassTheSharedItemComparisonBudget() {
        DataCompiler compiler = new DataCompiler();
        compiler.pushScope();
        DataCompiler.Binding left = compiler.declareLocal("left", DataType.ITEM_KEY);
        DataCompiler.Binding right = compiler.declareLocal("right", DataType.ITEM_KEY);
        DataExpression comparison = compiler.compile("$left != $right", 1, 0);
        QueryBudget budget = new QueryBudget();
        DataRuntime runtime = new DataRuntime(null, compiler.getMaxLocals(), null, budget);
        left.write(runtime, DataValue.itemIdentity(key("alias:item", 7, tag("same"))));
        right.write(runtime, DataValue.itemIdentity(key("alias:item", 7, tag("same"))));
        budget.chargeSlots(QueryBudget.MAX_SLOT_WORK);
        assertThrows(IllegalArgumentException.class, () -> comparison.evaluate(runtime));
    }

    @Test
    void equalityAndInequalityRejectMixedTypesAndOpaqueOrCollectionValues() {
        for (String expression : Arrays.asList(
            "1 != TRUE",
            "\"1\" != 1",
            "FALSE == 0",
            "[1] != [1]",
            "[1] == [1]",
            "{a:1}!={a:1}",
            "ITEM(\"*\")!=ITEM(\"*\")",
            "LABEL(\"a\")==LABEL(\"a\")",
            "!1",
            "TRUE && 1",
            "0 || FALSE",
            "1!==2",
            "TRUE&&&FALSE",
            "FALSE|||TRUE")) {
            assertThrows(
                IllegalArgumentException.class,
                () -> new DataCompiler().compile(expression, 1, 0),
                expression);
            assertFalse(
                new ProgramBuilder("EVERY 20 TICKS DO LET $bad = " + expression + " END").build()
                    .isSuccess(),
                expression);
        }
    }

    @Test
    void doubleEqualsNeverBecomesADeclarationOrAssignmentDelimiter() {
        for (String source : Arrays.asList(
            "STATE $x == 1 EVERY 20 TICKS DO END",
            "EVERY 20 TICKS DO LET $x == 1 END",
            "STATE $x = 1 EVERY 20 TICKS DO SET $x == 2 END"))
            assertFalse(
                new ProgramBuilder(source).build()
                    .isSuccess(),
                source);
        program("STATE $x = 1 == 1 EVERY 20 TICKS DO LET $n = 7 SET $x = $n != 8 END");
    }

    @Test
    void legacyHasAndRedstoneAcceptTheSameComparisonsAndBooleanAliases() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, new ItemStack(ITEM, 7));
        Program program = program(
            "STATE $r = 0 EVERY 20 TICKS DO " + "IF source HAS != 7 THEN SET $r = 99 END "
                + "IF source HAS != 6 THEN SET $r = $r + 1 END "
                + "IF REDSTONE != 0 THEN SET $r = 99 END "
                + "IF REDSTONE != 1 THEN SET $r = $r + 2 END "
                + "IF !(source HAS != 7) && REDSTONE == 0 THEN SET $r = $r + 4 END "
                + "IF FALSE || source HAS == 7 THEN SET $r = $r + 8 END END");
        Memory memory = new Memory(program);
        ProgramContext context = new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        context.setStateAccess(memory);
        program.tick(context);
        assertEquals(15, memory.value.asLong());
        assertEquals(7, count(fixture.source));
        assertEquals(0, count(fixture.dest));
    }

    @Test
    void comparisonEnumAndLegacyProgramTextRoundTripWithoutEnumNames() {
        assertSame(ComparisonOperator.NOT_EQUALS, ComparisonOperator.from("!="));
        assertSame(ComparisonOperator.EQUALS, ComparisonOperator.from("=="));
        assertEquals("!=", ComparisonOperator.NOT_EQUALS.getSymbol());
        assertTrue(ComparisonOperator.NOT_EQUALS.test(Long.MIN_VALUE, Long.MAX_VALUE));
        assertFalse(ComparisonOperator.NOT_EQUALS.test(Long.MIN_VALUE, Long.MIN_VALUE));
        Program program = program(
            "EVERY 20 TICKS DO IF !(source HAS != 7) && REDSTONE == 0 || REDSTONE != 1 THEN END END");
        String serialized = program.toString();
        assertTrue(serialized.contains("HAS !="));
        assertTrue(serialized.contains("REDSTONE !="));
        assertFalse(serialized.contains("NOT_EQUALS"));
        assertFalse(serialized.contains("EQUALS"));
        assertEquals(serialized, program(serialized).toString());
    }

    @Test
    void lexerAndHighlighterKeepLogicalPairsSeparateFromBitwiseSingles() {
        String operators = "! != == && || & | ~";
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(operators));
        lexer.removeErrorListeners();
        List<Token> tokens = new ArrayList<>();
        for (Token token : lexer.getAllTokens()) if (token.getChannel() == Token.DEFAULT_CHANNEL) tokens.add(token);
        int[] expected = { SFMLLexer.NOT, SFMLLexer.NE_SYMBOL, SFMLLexer.EQ_ALIAS, SFMLLexer.AND, SFMLLexer.OR,
            SFMLLexer.BIT_AND, SFMLLexer.BIT_OR, SFMLLexer.BIT_NOT };
        assertEquals(expected.length, tokens.size());
        for (int i = 0; i < expected.length; i++) assertEquals(
            expected[i],
            tokens.get(i)
                .getType());
        SfmlHighlighter.Highlighted highlighted = SfmlHighlighter.highlight(operators + "\n\"!= && !\" -- || ==");
        for (int i = 0; i < operators.length(); i++) if (operators.charAt(i) != ' ') assertEquals(
            SfmlHighlighter.Style.OPERATOR,
            highlighted.lines.get(0)
                .styleAt(i),
            "column " + i);
        assertEquals(
            SfmlHighlighter.Style.STRING,
            highlighted.lines.get(1)
                .styleAt(1));
        assertEquals(
            SfmlHighlighter.Style.COMMENT,
            highlighted.lines.get(1)
                .styleAt(13));
    }

    private static DataValue evaluate(String expression, DataQueries queries) {
        return new DataCompiler().compile(expression, 1, 0)
            .evaluate(new DataRuntime(null, 0, queries));
    }

    private static boolean compareKeys(String operator, ItemIdentity a, ItemIdentity b) {
        DataCompiler compiler = new DataCompiler();
        compiler.pushScope();
        DataCompiler.Binding left = compiler.declareLocal("left", DataType.ITEM_KEY);
        DataCompiler.Binding right = compiler.declareLocal("right", DataType.ITEM_KEY);
        DataExpression comparison = compiler.compile("$left " + operator + " $right", 1, 0);
        DataRuntime runtime = new DataRuntime(null, compiler.getMaxLocals());
        left.write(runtime, DataValue.itemIdentity(a));
        right.write(runtime, DataValue.itemIdentity(b));
        return comparison.evaluate(runtime)
            .asBoolean();
    }

    private static ItemIdentity key(String registry, int metadata, NBTTagCompound nbt) {
        return ItemIdentity.of(registry, metadata, nbt, new QueryBudget());
    }

    private static NBTTagCompound tag(String value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name", value);
        return tag;
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static final class Memory implements StateAccess {

        DataValue value;

        Memory(Program program) {
            value = program.getStateSchema()
                .getDeclarations()
                .get(0)
                .getDefaultValue();
        }

        @Override
        public DataValue read(int slot) {
            return value;
        }

        @Override
        public void write(int slot, DataValue next) {
            value = next;
        }
    }

    private static final class RecordingQueries implements DataQueries {

        final List<String> calls = new ArrayList<>();

        @Override
        public long count(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            calls.add(label.getName());
            return label.getName()
                .equals("left") ? 6 : 3;
        }

        @Override
        public boolean empty(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("Unexpected empty query");
        }

        @Override
        public boolean available(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("Unexpected available query");
        }
    }
}
