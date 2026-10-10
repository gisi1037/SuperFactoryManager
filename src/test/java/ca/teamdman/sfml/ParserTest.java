package ca.teamdman.sfml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.BoolConjunction;
import ca.teamdman.sfml.ast.BoolHas;
import ca.teamdman.sfml.ast.BoolNegation;
import ca.teamdman.sfml.ast.BoolRedstone;
import ca.teamdman.sfml.ast.ComparisonOperator;
import ca.teamdman.sfml.ast.ForgetStatement;
import ca.teamdman.sfml.ast.IfStatement;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.PulseTrigger;
import ca.teamdman.sfml.ast.RedstoneOutputStatement;
import ca.teamdman.sfml.ast.ResourceIdentifier;
import ca.teamdman.sfml.ast.RoundRobin;
import ca.teamdman.sfml.ast.Side;
import ca.teamdman.sfml.ast.TimerTrigger;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

public class ParserTest {

    @Test
    public void testBasicInputOutput() {
        String code = "EVERY 20 TICKS DO\n" + "  INPUT FROM chest\n" + "  OUTPUT TO furnace\n" + "END";
        ProgramBuildResult result = new ProgramBuilder(code).build();
        assertTrue(result.isSuccess(), "Errors: " + result.getErrors());
        Program program = result.getProgram();
        assertEquals(
            1,
            program.getTriggers()
                .size());
        assertTrue(
            program.getTriggers()
                .get(0) instanceof TimerTrigger);
        TimerTrigger timer = (TimerTrigger) program.getTriggers()
            .get(0);
        assertEquals(
            20,
            timer.getInterval()
                .getTicks());
        assertEquals(
            2,
            timer.getBlock()
                .getStatements()
                .size());
        assertTrue(
            timer.getBlock()
                .getStatements()
                .get(0) instanceof InputStatement);
        assertTrue(
            timer.getBlock()
                .getStatements()
                .get(1) instanceof OutputStatement);
    }

    @Test
    public void testSidednessAndSlots() {
        String code = "EVERY 1 SECONDS DO\n" + "  INPUT FROM chest TOP SIDE SLOTS 0-8\n"
            + "  OUTPUT TO furnace BOTTOM SIDE\n"
            + "END";
        ProgramBuildResult result = new ProgramBuilder(code).build();
        assertTrue(result.isSuccess(), "Errors: " + result.getErrors());
        Program program = result.getProgram();
        TimerTrigger timer = (TimerTrigger) program.getTriggers()
            .get(0);
        assertEquals(
            20,
            timer.getInterval()
                .getTicks()); // 1 second = 20 ticks

        InputStatement in = (InputStatement) timer.getBlock()
            .getStatements()
            .get(0);
        assertTrue(
            in.getLabelAccess()
                .getSides()
                .getSides()
                .contains(Side.TOP));
        assertNotNull(
            in.getLabelAccess()
                .getSlots());
        assertTrue(
            in.getLabelAccess()
                .getSlots()
                .contains(0));
        assertTrue(
            in.getLabelAccess()
                .getSlots()
                .contains(8));
        assertFalse(
            in.getLabelAccess()
                .getSlots()
                .contains(9));

        OutputStatement out = (OutputStatement) timer.getBlock()
            .getStatements()
            .get(1);
        assertTrue(
            out.getLabelAccess()
                .getSides()
                .getSides()
                .contains(Side.BOTTOM));
    }

    @Test
    public void testLimitsAndRetention() {
        String code = "EVERY 10 TICKS DO\n" + "  INPUT 64 RETAIN 16 minecraft:iron_ingot FROM chest\n"
            + "  OUTPUT 32 TO chest2\n"
            + "END";
        ProgramBuildResult result = new ProgramBuilder(code).build();
        assertTrue(result.isSuccess(), "Errors: " + result.getErrors());
        Program program = result.getProgram();
        TimerTrigger timer = (TimerTrigger) program.getTriggers()
            .get(0);
        InputStatement in = (InputStatement) timer.getBlock()
            .getStatements()
            .get(0);
        assertNotNull(in.getResourceLimits());
        assertEquals(
            1,
            in.getResourceLimits()
                .getLimits()
                .size());
        assertEquals(
            64,
            in.getResourceLimits()
                .getLimits()
                .get(0)
                .getLimit()
                .getQuantity()
                .getAmount());
        assertEquals(
            16,
            in.getResourceLimits()
                .getLimits()
                .get(0)
                .getLimit()
                .getRetention()
                .getAmount());
        assertEquals(
            "minecraft",
            in.getResourceLimits()
                .getLimits()
                .get(0)
                .getResourceIds()
                .getIdentifiers()
                .get(0)
                .getNamespace());
        assertEquals(
            "iron_ingot",
            in.getResourceLimits()
                .getLimits()
                .get(0)
                .getResourceIds()
                .getIdentifiers()
                .get(0)
                .getName());
    }

    @Test
    public void testRoundRobin() {
        String code = "EVERY 5 TICKS DO\n" + "  INPUT FROM chest\n"
            + "  OUTPUT TO furnace1, furnace2 ROUND ROBIN BY LABEL\n"
            + "END";
        ProgramBuildResult result = new ProgramBuilder(code).build();
        assertTrue(result.isSuccess(), "Errors: " + result.getErrors());
        Program program = result.getProgram();
        TimerTrigger timer = (TimerTrigger) program.getTriggers()
            .get(0);
        OutputStatement out = (OutputStatement) timer.getBlock()
            .getStatements()
            .get(1);
        assertTrue(
            out.getLabelAccess()
                .getRoundRobin()
                .isEnabled());
        assertEquals(
            RoundRobin.Mode.BY_LABEL,
            out.getLabelAccess()
                .getRoundRobin()
                .getMode());
        assertEquals(
            2,
            out.getLabelAccess()
                .getLabels()
                .size());
    }

    @Test
    public void testRoundRobinAlternationByLabel() {
        String code = "EVERY 20 TICKS DO\n" + "  INPUT FROM chest\n"
            + "  OUTPUT TO furnace1, furnace2 ROUND ROBIN BY LABEL\n"
            + "END";
        ProgramBuildResult res = new ProgramBuilder(code).build();
        assertTrue(res.isSuccess());
        OutputStatement out = (OutputStatement) ((TimerTrigger) res.getProgram()
            .getTriggers()
            .get(0)).getBlock()
                .getStatements()
                .get(1);

        LabelPositionHolder holder = new LabelPositionHolder();
        holder.addPosition("furnace1", new BlockPos(1, 0, 0));
        holder.addPosition("furnace2", new BlockPos(2, 0, 0));

        // When queried across statements, it alternates 1, 2, 1, 2
        assertEquals(
            new BlockPos(1, 0, 0),
            out.getLabelAccess()
                .getPositions(holder)
                .get(0));
        assertEquals(
            new BlockPos(2, 0, 0),
            out.getLabelAccess()
                .getPositions(holder)
                .get(0));
        assertEquals(
            new BlockPos(1, 0, 0),
            out.getLabelAccess()
                .getPositions(holder)
                .get(0));
        assertEquals(
            new BlockPos(2, 0, 0),
            out.getLabelAccess()
                .getPositions(holder)
                .get(0));
    }

    @Test
    public void testRoundRobinAlternationByBlock() {
        String code = "EVERY 20 TICKS DO\n" + "  INPUT FROM chest\n"
            + "  OUTPUT TO chests ROUND ROBIN BY BLOCK\n"
            + "END";
        ProgramBuildResult res = new ProgramBuilder(code).build();
        assertTrue(res.isSuccess());
        OutputStatement out = (OutputStatement) ((TimerTrigger) res.getProgram()
            .getTriggers()
            .get(0)).getBlock()
                .getStatements()
                .get(1);

        LabelPositionHolder holder = new LabelPositionHolder();
        holder.addPosition("chests", new BlockPos(10, 0, 0));
        holder.addPosition("chests", new BlockPos(20, 0, 0));

        // Alternates across blocks registered under the same label
        assertEquals(
            new BlockPos(10, 0, 0),
            out.getLabelAccess()
                .getPositions(holder)
                .get(0));
        assertEquals(
            new BlockPos(20, 0, 0),
            out.getLabelAccess()
                .getPositions(holder)
                .get(0));
        assertEquals(
            new BlockPos(10, 0, 0),
            out.getLabelAccess()
                .getPositions(holder)
                .get(0));
        assertEquals(
            new BlockPos(20, 0, 0),
            out.getLabelAccess()
                .getPositions(holder)
                .get(0));
    }

    @Test
    public void testIfConditionsAndRedstone() {
        String code = "EVERY REDSTONE PULSE DO\n" + "  IF chest HAS GT 10 WITH #ingotIron AND REDSTONE > 0 THEN\n"
            + "    INPUT FROM chest\n"
            + "    OUTPUT TO barrel\n"
            + "  ELSE\n"
            + "    FORGET\n"
            + "  END\n"
            + "END";
        ProgramBuildResult result = new ProgramBuilder(code).build();
        assertTrue(result.isSuccess(), "Errors: " + result.getErrors());
        Program program = result.getProgram();
        assertTrue(
            program.getTriggers()
                .get(0) instanceof PulseTrigger);
        PulseTrigger pulse = (PulseTrigger) program.getTriggers()
            .get(0);
        assertEquals(
            1,
            pulse.getBlock()
                .getStatements()
                .size());
        assertTrue(
            pulse.getBlock()
                .getStatements()
                .get(0) instanceof IfStatement);

        IfStatement ifStmt = (IfStatement) pulse.getBlock()
            .getStatements()
            .get(0);
        assertTrue(ifStmt.getCondition() instanceof BoolConjunction);

        BoolConjunction conj = (BoolConjunction) ifStmt.getCondition();
        assertTrue(conj.getLeft() instanceof BoolHas);
        assertTrue(conj.getRight() instanceof BoolRedstone);

        BoolHas has = (BoolHas) conj.getLeft();
        assertEquals(
            1,
            has.getLabelAccess()
                .getLabels()
                .size());
        assertEquals(
            "chest",
            has.getLabelAccess()
                .getLabels()
                .get(0)
                .getName());
        assertEquals(ComparisonOperator.GREATER, has.getComparisonOperator());
        assertEquals(10, has.getQuantity());
        assertNotNull(has.getWith());

        assertEquals(
            2,
            ifStmt.getTrueBlock()
                .getStatements()
                .size());
        assertNotNull(ifStmt.getFalseBlock());
        assertEquals(
            1,
            ifStmt.getFalseBlock()
                .getStatements()
                .size());
        assertTrue(
            ifStmt.getFalseBlock()
                .getStatements()
                .get(0) instanceof ForgetStatement);
    }

    @Test
    public void testFluidSyntax() {
        String code = "EVERY 10 TICKS DO\n" + "  INPUT 1000 fluid:water FROM tank\n" + "  OUTPUT TO boiler\n" + "END";
        ProgramBuildResult result = new ProgramBuilder(code).build();
        assertTrue(result.isSuccess(), "Errors: " + result.getErrors());
        Program program = result.getProgram();
        TimerTrigger timer = (TimerTrigger) program.getTriggers()
            .get(0);
        InputStatement in = (InputStatement) timer.getBlock()
            .getStatements()
            .get(0);
        assertNotNull(in.getResourceLimits());
        assertEquals(
            ResourceIdentifier.Type.FLUID,
            in.getResourceLimits()
                .getLimits()
                .get(0)
                .getResourceIds()
                .getIdentifiers()
                .get(0)
                .getType());
        assertEquals(
            "water",
            in.getResourceLimits()
                .getLimits()
                .get(0)
                .getResourceIds()
                .getIdentifiers()
                .get(0)
                .getName());
    }

    @Test
    public void testCommentsAndGlobalTrigger() {
        String code = "-- Header comment\n" + "NAME \"Global Sorting\"\n"
            + "-- Another comment\n"
            + "EVERY 10 GLOBAL PLUS 2 TICKS DO\n"
            + "  -- Do something inside\n"
            + "  INPUT FROM input_chest\n"
            + "  OUTPUT TO EMPTY SLOTS IN output_chest\n"
            + "END\n";
        ProgramBuildResult result = new ProgramBuilder(code).build();
        assertTrue(result.isSuccess(), "Errors: " + result.getErrors());
        Program program = result.getProgram();
        assertEquals("Global Sorting", program.getName());
        TimerTrigger timer = (TimerTrigger) program.getTriggers()
            .get(0);
        assertEquals(
            10,
            timer.getInterval()
                .getTicks());
        assertTrue(
            timer.getInterval()
                .isGlobal());
        assertEquals(
            2,
            timer.getInterval()
                .getOffset());
        OutputStatement out = (OutputStatement) timer.getBlock()
            .getStatements()
            .get(1);
        assertTrue(out.isEmptySlotsOnly());
    }

    @Test
    public void testNegatedCondition() {
        String code = "EVERY 20 TICKS DO\n" + "  IF NOT chest HAS > 0 THEN\n"
            + "    INPUT FROM backup_chest\n"
            + "    OUTPUT TO chest\n"
            + "  END\n"
            + "END";
        ProgramBuildResult result = new ProgramBuilder(code).build();
        assertTrue(result.isSuccess(), "Errors: " + result.getErrors());
        Program program = result.getProgram();
        TimerTrigger timer = (TimerTrigger) program.getTriggers()
            .get(0);
        IfStatement ifStmt = (IfStatement) timer.getBlock()
            .getStatements()
            .get(0);
        assertTrue(ifStmt.getCondition() instanceof BoolNegation);
    }

    @Test
    public void testSyntaxErrorHandling() {
        String badCode = "EVERY INVALID SYNTAX DO INPUT";
        ProgramBuildResult result = new ProgramBuilder(badCode).build();
        assertFalse(result.isSuccess());
        assertFalse(
            result.getErrors()
                .isEmpty());
    }

    @Test
    public void testRedstoneOutputStatements() {
        String code = "EVERY 20 TICKS DO\n" + "  OUTPUT 15 REDSTONE LEVEL TO emitter\n"
            + "  OUTPUT REDSTONE LEVEL 12 TO emitter\n"
            + "  OUTPUT 0 REDSTONE LEVEL TO emitter TOP SIDE\n"
            + "  OUTPUT REDSTONE LEVEL TO EACH emitter\n"
            + "  OUTPUT 15 REDSTONE TO chest\n"
            + "END";
        ProgramBuildResult result = new ProgramBuilder(code).build();
        assertTrue(result.isSuccess(), "Errors: " + result.getErrors());
        Program program = result.getProgram();
        TimerTrigger timer = (TimerTrigger) program.getTriggers()
            .get(0);
        assertEquals(
            5,
            timer.getBlock()
                .getStatements()
                .size());

        // 1. OUTPUT 15 REDSTONE LEVEL TO emitter
        assertTrue(
            timer.getBlock()
                .getStatements()
                .get(0) instanceof RedstoneOutputStatement);
        RedstoneOutputStatement ros1 = (RedstoneOutputStatement) timer.getBlock()
            .getStatements()
            .get(0);
        assertEquals(15, ros1.getLevel());
        assertFalse(ros1.isEach());

        // 2. OUTPUT REDSTONE LEVEL 12 TO emitter
        assertTrue(
            timer.getBlock()
                .getStatements()
                .get(1) instanceof RedstoneOutputStatement);
        RedstoneOutputStatement ros2 = (RedstoneOutputStatement) timer.getBlock()
            .getStatements()
            .get(1);
        assertEquals(12, ros2.getLevel());
        assertFalse(ros2.isEach());

        // 3. OUTPUT 0 REDSTONE LEVEL TO emitter TOP SIDE
        assertTrue(
            timer.getBlock()
                .getStatements()
                .get(2) instanceof RedstoneOutputStatement);
        RedstoneOutputStatement ros3 = (RedstoneOutputStatement) timer.getBlock()
            .getStatements()
            .get(2);
        assertEquals(0, ros3.getLevel());
        assertTrue(
            ros3.getLabelAccess()
                .getSides()
                .getSides()
                .contains(Side.TOP));

        // 4. OUTPUT REDSTONE LEVEL TO EACH emitter (defaults to level 15)
        assertTrue(
            timer.getBlock()
                .getStatements()
                .get(3) instanceof RedstoneOutputStatement);
        RedstoneOutputStatement ros4 = (RedstoneOutputStatement) timer.getBlock()
            .getStatements()
            .get(3);
        assertEquals(15, ros4.getLevel());
        assertTrue(ros4.isEach());

        // 5. OUTPUT 15 REDSTONE TO chest (item transfer of redstone dust)
        assertTrue(
            timer.getBlock()
                .getStatements()
                .get(4) instanceof OutputStatement);
    }
}
