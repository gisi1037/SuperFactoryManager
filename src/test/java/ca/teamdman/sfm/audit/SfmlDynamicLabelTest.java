package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ExpressionLimits;

class SfmlDynamicLabelTest {

    @Test
    void integerConcatenationUsesSignedDecimalInBothDirections() {
        assertEquals("chest1", evaluate("\"chest\" + 1").asString());
        assertEquals("1chest", evaluate("1 + \"chest\"").asString());
        assertEquals("v-9223372036854775808", evaluate("\"v\" + -9223372036854775808").asString());
        assertEquals("9223372036854775807v", evaluate("9223372036854775807 PLUS \"v\"").asString());
        assertEquals("0", evaluate("\"\" + 0").asString());
        assertEquals("3x", evaluate("1 + 2 + \"x\"").asString());
        assertEquals("x12", evaluate("\"x\" + 1 + 2").asString());
        assertEquals(3, evaluate("1 + 2").asLong());
    }

    @Test
    void mixedConcatenationDoesNotRelaxOtherTypesOrComparisons() {
        for (String expression : Arrays.asList(
            "\"x\" + TRUE",
            "FALSE + \"x\"",
            "\"1\" = 1",
            "1 != \"1\"",
            "\"1\" < 1",
            "\"x\" - 1",
            "\"x\" + [1]",
            "LABEL(\"x\") + \"y\"",
            "LABEL(1)")) assertThrows(IllegalArgumentException.class, () -> evaluate(expression), expression);
    }

    @Test
    void computedNameIsEvaluatedFromCurrentBindingAndPreservesExactCase() {
        DataCompiler compiler = compiler();
        DataCompiler.Binding index = compiler.declareLocal("index", DataType.INTEGER);
        DataRuntime runtime = new DataRuntime(null, 1);
        DataExpression label = compiler.compile("label(\"Chest\" + $index).side(\"up\").slots(1, 2)", 1, 0);
        index.write(runtime, DataValue.integer(1));
        assertEquals(
            "Chest1",
            label.evaluate(runtime)
                .asLabel()
                .getName());
        assertEquals(
            2,
            label.evaluate(runtime)
                .asLabel()
                .getSideMask());
        assertEquals(
            1,
            label.evaluate(runtime)
                .asLabel()
                .getFirstSlot());
        index.write(runtime, DataValue.integer(2));
        assertEquals(
            "Chest2",
            label.evaluate(runtime)
                .asLabel()
                .getName());
        assertTrue(compiler.hasDynamicLabels());
        assertTrue(
            compiler.getReferencedLabels()
                .isEmpty());
    }

    @Test
    void constantExpressionsRetainTheSelectiveLiteralDependencyPath() {
        DataCompiler compiler = new DataCompiler();
        assertEquals(
            "Chest3",
            compiler.compile("LABEL(\"Chest\" + (1 + 2))", 1, 0)
                .evaluate(new DataRuntime(null, 0))
                .asLabel()
                .getName());
        assertEquals(Collections.singleton("Chest3"), compiler.getReferencedLabels());
        assertFalse(compiler.hasDynamicLabels());
    }

    @Test
    void dynamicNamesHaveTheSameBoundsAsLiteralNames() {
        DataCompiler compiler = compiler();
        DataCompiler.Binding name = compiler.declareLocal("name", DataType.STRING);
        DataExpression expression = compiler.compile("LABEL($name)", 7, 2);
        DataRuntime runtime = new DataRuntime(null, 1);
        String maximum = new String(new char[256]).replace('\0', 'a');
        name.write(runtime, DataValue.string(maximum));
        assertEquals(
            maximum,
            expression.evaluate(runtime)
                .asLabel()
                .getName());
        for (String invalid : Arrays.asList("", " chest", "chest ", maximum + "x")) {
            name.write(runtime, DataValue.string(invalid));
            IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> expression.evaluate(runtime));
            assertTrue(
                failure.getMessage()
                    .contains("Line 7, Col 2"),
                failure.getMessage());
            assertThrows(
                IllegalArgumentException.class,
                () -> new DataCompiler().compile("LABEL(" + DataValue.string(invalid) + ")", 1, 0));
        }
    }

    @Test
    void numericConcatenationKeepsStringSizeAndAllocationBudgets() {
        DataCompiler compiler = compiler();
        DataCompiler.Binding text = compiler.declareLocal("text", DataType.STRING);
        DataCompiler.Binding number = compiler.declareLocal("number", DataType.INTEGER);
        DataRuntime runtime = new DataRuntime(null, 2);
        text.write(runtime, DataValue.string("chest"));
        number.write(runtime, DataValue.integer(Long.MIN_VALUE));
        assertEquals(
            "chest-9223372036854775808",
            compiler.compile("$text + $number", 1, 0)
                .evaluate(runtime)
                .asString());
        assertEquals(25, runtime.getAllocatedStringCharacters());
        text.write(
            runtime,
            DataValue.string(new String(new char[ExpressionLimits.MAX_STRING_LENGTH]).replace('\0', 'x')));
        assertThrows(
            IllegalArgumentException.class,
            () -> compiler.compile("$number + $text", 1, 0)
                .evaluate(runtime));
    }

    @Test
    void reachableFunctionDependenciesPropagateAndUnusedDefinitionsStaySelective() {
        String definitions = "FUNCTION make($n: INTEGER) RETURNS LABEL_REF DO RETURN LABEL(\"chest\" + $n) END "
            + "FUNCTION inner($n: INTEGER) RETURNS BOOLEAN DO RETURN make($n).empty() END "
            + "FUNCTION outer($n: INTEGER) RETURNS BOOLEAN DO RETURN inner($n) END ";
        Program unused = parse(definitions + "EVERY 1 TICKS DO LET $x = LABEL(\"source\").empty() END");
        assertFalse(unused.hasDynamicLabels());
        assertEquals(Collections.singleton("source"), unused.getReferencedLabels());
        Program called = parse(definitions + "EVERY 1 TICKS DO LET $x = outer(1) END");
        assertTrue(called.hasDynamicLabels());
        assertTrue(parse(called.toString()).hasDynamicLabels());
    }

    @Test
    void requestedExampleResolvesDiskGroupsDuringScheduledExecution() throws Exception {
        runDiskExample(false, false);
    }

    @Test
    void computedLabelsReturnedThroughPlayerFunctionsResolveDuringManualExecution() throws Exception {
        runDiskExample(true, true);
    }

    private void runDiskExample(boolean manual, boolean functions) throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        fixture.stock(0, 3);
        fixture.labels.addPosition("chest1", DST);
        // A different case must not accidentally merge into the dynamically selected empty group.
        fixture.labels.addPosition("Chest1", SRC);
        String definitions = functions
            ? "FUNCTION make($n: INTEGER) RETURNS LABEL_REF DO RETURN label(\"chest\" + $n) END "
                + "FUNCTION empty($n: INTEGER) RETURNS BOOLEAN DO RETURN make($n).empty() END "
            : "";
        String condition = functions ? "empty($index)" : "label(\"chest\" + $index).empty()";
        manager.saveCodeAndRecompile(
            definitions + "EVERY 1 TICKS DO let $index = 1 IF ("
                + condition
                + ") THEN INPUT FROM \"source\" OUTPUT 1 TO \"dest\" END END");
        LabelPositionHolder.saveTo(manager.getDisk(), fixture.labels);
        NBTTagCompound before = (NBTTagCompound) manager.getDisk()
            .getTagCompound()
            .copy();
        if (manual) manager.runOnceWithLogLevel(LogLevel.OFF);
        else manager.updateEntity();
        assertEquals(1, count(fixture.dest));
        assertEquals(2, count(fixture.source));
        assertEquals(
            before,
            manager.getDisk()
                .getTagCompound());
        // The receiver is observed afresh next invocation; the now occupied group is no longer empty.
        if (manual) manager.runOnceWithLogLevel(LogLevel.OFF);
        else manager.updateEntity();
        assertEquals(1, count(fixture.dest));
    }

    @Test
    void dynamicSnapshotFreezesAllNamesTogetherAndRefreshesOnNextInvocation() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        fixture.labels.addPosition("chest1", SRC);
        fixture.labels.addPosition("chest2", DST);
        LabelPositionHolder.saveTo(manager.getDisk(), fixture.labels);
        Program program = parse("EVERY 1 TICKS DO LET $index = 1 LET $label = LABEL(\"chest\" + $index) END");
        ProgramContext first = context(fixture, manager, program);
        program.tick(first);
        assertEquals(
            Collections.singleton(SRC),
            first.getLabelPositionHolder()
                .getPositions("chest1"));
        int[] coordinates = manager.getDisk()
            .getTagCompound()
            .getCompoundTag(LabelPositionHolder.NBT_KEY)
            .getTagList("chest2", 11)
            .func_150306_c(0);
        coordinates[0] = EXTRA.x;
        coordinates[1] = EXTRA.y;
        coordinates[2] = EXTRA.z;
        assertEquals(
            Collections.singleton(DST),
            first.getLabelPositionHolder()
                .getPositions("chest2"));
        ProgramContext next = context(fixture, manager, program);
        program.tick(next);
        assertEquals(
            Collections.singleton(EXTRA),
            next.getLabelPositionHolder()
                .getPositions("chest2"));
    }

    @Test
    void computedNameCanResumeAFunctionAndUsesTheCurrentInvocationDiskSnapshot() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        fixture.stock(0, 3);
        fixture.labels.addPosition("chest1", SRC);
        LabelPositionHolder.saveTo(manager.getDisk(), fixture.labels);
        Program program = parse(
            "FUNCTION later($n: INTEGER) RETURNS INTEGER DO wait(2) RETURN $n END "
                + "EVERY 10 TICKS DO LET $index = 1 IF (label(\"chest\" + later($index)).empty()) THEN "
                + "INPUT FROM \"source\" OUTPUT 1 TO \"dest\" END END");
        fixture.world.time = 10;
        program.tick(
            new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                null,
                10,
                false,
                program.getReferencedLabels()));
        assertEquals(0, count(fixture.dest));
        fixture.labels.removePosition("chest1", SRC);
        fixture.labels.addPosition("chest1", DST);
        LabelPositionHolder.saveTo(manager.getDisk(), fixture.labels);
        fixture.world.time = 12;
        program.tick(
            new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                null,
                12,
                false,
                program.getReferencedLabels()));
        assertEquals(1, count(fixture.dest));
        assertEquals(2, count(fixture.source));
    }

    @Test
    void oversizedSnapshotsFailExplicitlyAndLeaveLiteralSelectionUsable() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        NBTTagCompound labels = new NBTTagCompound();
        manager.getDisk()
            .setTagCompound(new NBTTagCompound());
        manager.getDisk()
            .getTagCompound()
            .setTag(LabelPositionHolder.NBT_KEY, labels);
        for (int i = 0; i < LabelPositionHolder.MAX_SNAPSHOT_LABELS; i++) labels.setTag("unused" + i, new NBTTagList());
        assertEquals(
            LabelPositionHolder.MAX_SNAPSHOT_LABELS,
            LabelPositionHolder.boundedSnapshot(manager.getDisk())
                .getAllLabels()
                .size());
        labels.setTag("extra", new NBTTagList());
        assertThrows(IllegalArgumentException.class, () -> LabelPositionHolder.boundedSnapshot(manager.getDisk()));
        assertEquals(
            Collections.singleton("extra"),
            LabelPositionHolder.from(manager.getDisk(), Collections.singleton("extra"))
                .getAllLabels()
                .keySet());

        labels = new NBTTagCompound();
        manager.getDisk()
            .getTagCompound()
            .setTag(LabelPositionHolder.NBT_KEY, labels);
        NBTTagList positions = new NBTTagList();
        for (int i = 0; i < LabelPositionHolder.MAX_SNAPSHOT_POSITIONS; i++)
            positions.appendTag(new NBTTagIntArray(new int[] { i, 64, 0 }));
        labels.setTag("one", positions);
        assertEquals(
            LabelPositionHolder.MAX_SNAPSHOT_POSITIONS,
            LabelPositionHolder.boundedSnapshot(manager.getDisk())
                .getPositions("one")
                .size());
        positions.appendTag(new NBTTagIntArray(new int[] { -1, 64, 0 }));
        assertThrows(IllegalArgumentException.class, () -> LabelPositionHolder.boundedSnapshot(manager.getDisk()));

        labels = new NBTTagCompound();
        manager.getDisk()
            .getTagCompound()
            .setTag(LabelPositionHolder.NBT_KEY, labels);
        String longName = new String(new char[256]).replace('\0', 'x');
        for (int i = 0; i <= LabelPositionHolder.MAX_SNAPSHOT_NAME_CHARACTERS / longName.length(); i++)
            labels.setTag(longName + i, new NBTTagList());
        assertThrows(IllegalArgumentException.class, () -> LabelPositionHolder.boundedSnapshot(manager.getDisk()));
    }

    private ProgramContext context(Fixture fixture, TileEntityManager manager, Program program) {
        return new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            null,
            1,
            false,
            program.getReferencedLabels());
    }

    private static DataCompiler compiler() {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        return compiler;
    }

    private static DataValue evaluate(String expression) {
        return new DataCompiler().compile(expression, 1, 0)
            .evaluate(new DataRuntime(null, 0));
    }
}
