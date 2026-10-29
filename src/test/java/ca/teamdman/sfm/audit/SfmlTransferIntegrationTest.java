package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.TransferStatement;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport;

class SfmlTransferIntegrationTest {

    private static final Item TEST_ITEM = new Item();
    private static final String REQUEST = "TRANSFER (LABEL(\"source\"), LABEL(\"dest\"), ITEM(\"*\"), ";
    static {
        try {
            Field backing = RegistrySimple.class.getDeclaredField("registryObjects");
            backing.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) backing.get(Item.itemRegistry);
            names.put("sfm_transfer_language:item", TEST_ITEM);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void twoRequestsUseLiveStockAndExposeTypedResults() throws Exception {
        Fixture fixture = fixture(7);
        Program program = program(
            "STATE $result = {first:0,second:0,left:0,requested:0,status:\"\"} EVERY 20 TICKS DO " + REQUEST
                + "5) INTO $a "
                + REQUEST
                + "5) INTO $b "
                + "SET $result.first = $a.moved SET $result.second = $b.moved SET $result.left = $b.remaining "
                + "SET $result.requested = $b.requested SET $result.status = $b.status END");
        Memory memory = new Memory(program);
        program.tick(context(fixture, memory));
        assertEquals(
            5,
            memory.value.get("first")
                .asLong());
        assertEquals(
            2,
            memory.value.get("second")
                .asLong());
        assertEquals(
            3,
            memory.value.get("left")
                .asLong());
        assertEquals(
            5,
            memory.value.get("requested")
                .asLong());
        assertEquals(
            "partial",
            memory.value.get("status")
                .asString());
        assertEquals(0, count(fixture.source));
        assertEquals(7, count(fixture.dest));
    }

    @Test
    void capturedExactKeyCanDriveActionInsideGroupIteration() throws Exception {
        Fixture fixture = fixture(9);
        Program program = program(
            "STATE $moved = 0 EVERY 20 TICKS DO LET $source = LABEL(\"source\") "
                + "LET $stock = $source.contents() FOR $group IN $stock DO "
                + "TRANSFER ($source, LABEL(\"dest\"), $group.item, MIN($group.total, 4)) INTO $sent "
                + "SET $moved = $moved + $sent.moved END END");
        Memory memory = new Memory(program);
        program.tick(context(fixture, memory));
        assertEquals(4, memory.value.asLong());
        assertEquals(5, count(fixture.source));
        assertEquals(4, count(fixture.dest));
    }

    @Test
    void helpersReadOneSnapshotAndCanPlanAStockTarget() throws Exception {
        Fixture fixture = fixture(9);
        Program program = program(
            "STATE $result = {total:0,stock:0,places:0,short:0,moved:0} "
                + "FUNCTION observed($items: ITEM_CONTENTS) RETURNS INTEGER DO RETURN TOTAL($items) END "
                + "EVERY 20 TICKS DO LET $stock = LABEL(\"source\").contents() "
                + "SET $result.total = observed($stock) SET $result.stock = STOCK($stock, $stock[0].item) "
                + "SET $result.places = TOTAL($stock[0].locations) "
                + "LET $missing = SHORTAGE(5, LABEL(\"dest\").count(ITEM(\"*\"))) "
                + "SET $result.short = $missing "
                + REQUEST
                + "$missing) INTO $sent SET $result.moved = $sent.moved END");
        Memory memory = new Memory(program);
        program.tick(context(fixture, memory));
        assertEquals(
            9,
            memory.value.get("total")
                .asLong());
        assertEquals(
            9,
            memory.value.get("stock")
                .asLong());
        assertEquals(
            9,
            memory.value.get("places")
                .asLong());
        assertEquals(
            5,
            memory.value.get("short")
                .asLong());
        assertEquals(
            5,
            memory.value.get("moved")
                .asLong());
        assertEquals(4, count(fixture.source));
        assertEquals(5, count(fixture.dest));
    }

    @Test
    void stockBuiltinTypesAndNonnegativeShortagesAreChecked() {
        for (String expression : Arrays.asList(
            "TOTAL(1)",
            "STOCK([1], 2)",
            "SHORTAGE(1, TRUE)",
            "SHORTAGE(1)",
            "TOTAL([1, 2])",
            "STOCK(LABEL(\"source\").contents(), ITEM(\"*\"))"))
            assertFalse(
                new ProgramBuilder("EVERY 20 TICKS DO LET $x = " + expression + " END").build()
                    .isSuccess(),
                expression);
        assertEquals(
            0,
            new DataCompiler().compile("SHORTAGE(4, 7)", 1, 0)
                .evaluate(new DataRuntime(null, 0))
                .asLong());
        assertThrows(
            IllegalArgumentException.class,
            () -> new DataCompiler().compile("SHORTAGE(-1, 7)", 1, 0)
                .evaluate(new DataRuntime(null, 0)));
        assertTrue(
            new ProgramBuilder("STATE $target = SHORTAGE(8, 3) EVERY 20 TICKS DO END").build()
                .isSuccess());
    }

    @Test
    void transferDoesNotRegisterForgetOrSpendLegacyInputAllowance() throws Exception {
        Fixture fixture = fixture(12);
        Program program = program(
            "EVERY 20 TICKS DO INPUT 2 FROM source " + REQUEST + "4) INTO $sent OUTPUT TO dest END");
        program.tick(context(fixture, null));
        assertEquals(6, count(fixture.source));
        assertEquals(6, count(fixture.dest));
    }

    @Test
    void nonzeroActionsRequireRecoveryAuthorityBeforeAnyProviderAccess() {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        assertThrows(
            IllegalStateException.class,
            () -> context.transferItems(
                LabelReference.literal("source"),
                LabelReference.literal("dest"),
                DataValue.selector(ResourceSelector.item("*")),
                1));
        assertEquals(
            0,
            context.getInvocationQueryBudget()
                .getProviderCalls());
    }

    @Test
    void zeroRequiresNoNativeWorldAndResultCanFeedPureFunctions() {
        Program program = program(
            "STATE $remaining = 7 FUNCTION left($r: {requested: INTEGER, moved: INTEGER, "
                + "remaining: INTEGER, status: STRING}) RETURNS INTEGER DO RETURN $r.remaining END "
                + "EVERY 20 TICKS DO "
                + REQUEST
                + "0) INTO $result SET $remaining = left($result) END");
        Memory memory = new Memory(program);
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setStateAccess(memory);
        program.tick(context);
        assertEquals(0, memory.value.asLong());
    }

    @Test
    void argumentsEvaluateExactlyOnceInSourceOrder() {
        DataCompiler compiler = new DataCompiler();
        compiler.pushScope();
        DataCompiler.Binding result = compiler.declareLocal("result", TransferStatement.RESULT_TYPE);
        List<Integer> order = new ArrayList<>();
        List<DataExpression> arguments = Arrays.asList(
            argument(0, DataValue.label(LabelReference.literal("source")), order),
            argument(1, DataValue.label(LabelReference.literal("dest")), order),
            argument(2, DataValue.selector(ResourceSelector.item("*")), order),
            argument(3, DataValue.integer(0), order));
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.beginDataInvocation(compiler.getMaxLocals(), false);
        new TransferStatement(arguments, result).tick(context);
        assertEquals(Arrays.asList(0, 1, 2, 3), order);
        assertEquals(
            "complete",
            context.getDataRuntime()
                .read(false, result.slot)
                .get("status")
                .asString());
    }

    @Test
    void expressionsAndActionsShareTheContextQueryOwner() {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.beginDataInvocation(0, false);
        assertDoesNotThrow(
            () -> context.getDataRuntime()
                .requireQueryBudget(context.getInvocationQueryBudget()));
    }

    @Test
    void resultPublicationBudgetIsReservedBeforeAction() {
        DataCompiler compiler = new DataCompiler();
        compiler.pushScope();
        DataCompiler.Binding result = compiler.declareLocal("result", TransferStatement.RESULT_TYPE);
        DataRuntime runtime = new DataRuntime(null, compiler.getMaxLocals());
        runtime.work(16384);
        AtomicInteger actions = new AtomicInteger();
        assertThrows(IllegalArgumentException.class, () -> result.writeActionResult(runtime, () -> {
            actions.incrementAndGet();
            return null;
        }));
        assertEquals(0, actions.get());
    }

    @Test
    void resultRecordBudgetFailureCannotMoveItems() throws Exception {
        Fixture fixture = fixture(9);
        Program program = program("EVERY 20 TICKS DO " + REQUEST + "4) INTO $sent END");
        ProgramContext context = context(fixture, null);
        context.beginDataInvocation(1, false);
        context.getDataRuntime()
            .allocate(16384);
        assertThrows(
            IllegalArgumentException.class,
            () -> program.getTriggers()
                .get(0)
                .getBlock()
                .tick(context));
        assertEquals(9, count(fixture.source));
        assertEquals(0, count(fixture.dest));
    }

    @Test
    void effectsAreRejectedInExpressionsDefaultsAndOrdinaryFunctions() {
        for (String body : Arrays.asList(
            "STATE $x = TRANSFER(LABEL(\"source\"), LABEL(\"dest\"), ITEM(\"*\"), 1) EVERY 20 TICKS DO END",
            "EVERY 20 TICKS DO LET $x = TRANSFER(LABEL(\"source\"), LABEL(\"dest\"), ITEM(\"*\"), 1) END",
            "EVERY 20 TICKS DO IF (TRANSFER(LABEL(\"source\"), LABEL(\"dest\"), ITEM(\"*\"), 1) = TRUE) THEN END END",
            "FUNCTION f() RETURNS INTEGER DO " + REQUEST + "1) INTO $x RETURN $x.moved END EVERY 20 TICKS DO END",
            "FUNCTION TRANSFER($x: INTEGER) RETURNS INTEGER DO RETURN $x END EVERY 20 TICKS DO END"))
            assertFalse(
                new ProgramBuilder(body).build()
                    .isSuccess(),
                body);
    }

    @Test
    void argumentTypesFreshLocalNamesAndLexicalScopeAreCheckedAtCompileTime() {
        for (String body : Arrays.asList(
            "TRANSFER (7, LABEL(\"dest\"), ITEM(\"*\"), 1) INTO $r",
            "TRANSFER (LABEL(\"source\"), 7, ITEM(\"*\"), 1) INTO $r",
            "TRANSFER (LABEL(\"source\"), LABEL(\"dest\"), 7, 1) INTO $r",
            REQUEST + "TRUE) INTO $r",
            REQUEST + "1) INTO $r " + REQUEST + "1) INTO $r",
            "IF TRUE THEN " + REQUEST + "1) INTO $r END PRINT($r.moved)"))
            assertFalse(
                new ProgramBuilder("EVERY 20 TICKS DO " + body + " END").build()
                    .isSuccess(),
                body);
    }

    @Test
    void publicLogisticsProgramsCompileWithTheCurrentLanguage() throws Exception {
        for (String example : Arrays.asList(
            "target_stock.sfml",
            "exact_variant.sfml",
            "infusion-current-methods.sfml",
            "infusion-current-legacy.sfml",
            "infusion-auto-nbt.sfml"))
            program(
                new String(Files.readAllBytes(Paths.get("examples", "logistics", example)), StandardCharsets.UTF_8));
    }

    @Test
    void transferWordsKeepResourceFieldAndLegacyLabelColors() {
        String source = "TRANSFER (LABEL(\"a\"), LABEL(\"b\"), ITEM(\"*\"), 0) INTO $r\n"
            + "INPUT minecraft:TRANSFER minecraft:INTO FROM TRANSFER OUTPUT TO INTO\n"
            + "LET $x = $record.TRANSFER + $record.INTO";
        SfmlHighlighter.Highlighted text = SfmlHighlighter.highlight(source);
        assertEquals(
            SfmlHighlighter.Style.ACTION,
            text.lines.get(0)
                .styleAt(0));
        assertEquals(
            SfmlHighlighter.Style.KEYWORD,
            text.lines.get(0)
                .styleAt(text.lines.get(0).text.indexOf("INTO")));
        for (String resource : Arrays.asList("minecraft:TRANSFER", "minecraft:INTO")) {
            int start = text.lines.get(1).text.indexOf(resource);
            for (int i = start; i < start + resource.length(); i++) assertEquals(
                SfmlHighlighter.Style.RESOURCE,
                text.lines.get(1)
                    .styleAt(i));
        }
        assertEquals(
            SfmlHighlighter.Style.LABEL,
            text.lines.get(1)
                .styleAt(text.lines.get(1).text.lastIndexOf("TRANSFER")));
        assertEquals(
            SfmlHighlighter.Style.LABEL,
            text.lines.get(1)
                .styleAt(text.lines.get(1).text.lastIndexOf("INTO")));
        assertEquals(
            SfmlHighlighter.Style.LABEL,
            text.lines.get(2)
                .styleAt(text.lines.get(2).text.indexOf("TRANSFER")));
        assertEquals(
            SfmlHighlighter.Style.LABEL,
            text.lines.get(2)
                .styleAt(text.lines.get(2).text.indexOf("INTO")));
    }

    @Test
    void reservedSpellingsStillWorkAsLegacyLabelsAndFixedFields() {
        program(
            "EVERY 20 TICKS DO INPUT FROM TRANSFER OUTPUT TO INTO "
                + "LET $row = {TRANSFER: 1, INTO: 2} PRINT($row.TRANSFER, $row.INTO) END");
    }

    @Test
    void actionRoundTripsAndIsExplicitlyBlockedFromVisualProjection() {
        String source = "EVERY 20 TICKS DO " + REQUEST + "2) INTO $result PRINT($result.moved) END";
        ProgramBuildResult result = new ProgramBuilder(source).buildWithSyntax();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(result);
        assertFalse(report.isAvailable());
        assertTrue(
            report.getBlockers()
                .stream()
                .anyMatch(b -> b.kind == VisualCapabilityReport.Kind.TRANSFER_STATEMENT));
        Program rebuilt = program(
            result.getProgram()
                .toString());
        assertTrue(
            rebuilt.getReferencedLabels()
                .containsAll(Arrays.asList("source", "dest")));
        assertEquals(
            result.getProgram()
                .toString(),
            rebuilt.toString());
        assertEquals(
            SfmlHighlighter.Style.ACTION,
            SfmlHighlighter.highlight("TRANSFER (").lines.get(0)
                .styleAt(0));
    }

    private static DataExpression argument(int index, DataValue value, List<Integer> order) {
        return new DataExpression() {

            @Override
            public DataType getType() {
                return value.getType();
            }

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                order.add(index);
                return value;
            }

            @Override
            public String toString() {
                return value.toString();
            }
        };
    }

    private static Fixture fixture(int amount) throws Exception {
        Fixture fixture = new Fixture();
        RecoveryFixtures.manager(fixture);
        fixture.source.setInventorySlotContents(0, new ItemStack(TEST_ITEM, amount));
        return fixture;
    }

    private static ProgramContext context(Fixture fixture, StateAccess state) {
        TileEntityManager manager = (TileEntityManager) fixture.world.tiles.get(MANAGER);
        ProgramContext context = new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        context.setStateAccess(state);
        return context;
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
}
