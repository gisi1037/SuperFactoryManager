package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport;

class SfmlBreakTest {

    @TestFactory
    Collection<DynamicTest> everyLoopExitsInTriggersAndFunctionsWithAndWithoutWait() {
        String[] headers = { "for i = 0 to 4", "for value in [1,2,3]", "for (index,value) in [1,2,3]",
            "for(let i=0;i<4;steps=steps+1)", "while(count<20)" };
        Collection<DynamicTest> tests = new ArrayList<>();
        for (String header : headers)
            for (boolean function : new boolean[] { false, true }) for (boolean sleep : new boolean[] { false, true })
                tests.add(DynamicTest.dynamicTest(header + " function=" + function + " wait=" + sleep, () -> {
                    String loop = header + " { count=count+1; "
                        + (sleep ? "wait(1); " : "")
                        + "break; count=count+100; } ";
                    String definitions = function
                        ? "integer choose(ref integer n, ref integer s) { " + loop.replace("count", "n")
                            .replace("steps", "s") + "return 7; } "
                        : "";
                    String body = function ? "let answer=choose(ref count,ref steps);" : loop;
                    Run run = new Run(
                        "state count=0 state steps=0 state completed=0 " + definitions
                            + "every 100 ticks { "
                            + body
                            + "completed=completed+1; }");
                    run.tick(100);
                    assertEquals(1, run.value(0));
                    assertEquals(0, run.value(1), "Java FOR update must be skipped");
                    assertEquals(sleep ? 0 : 1, run.value(2));
                    if (sleep) run.tick(101);
                    assertEquals(1, run.value(0), "Resume must not repeat earlier effects");
                    assertEquals(1, run.value(2));
                    assertEquals(0, run.sleeping());
                    run.tick(200);
                    if (sleep) run.tick(201);
                    assertEquals(2, run.value(0));
                    assertEquals(2, run.value(2));
                    assertEquals(0, run.sleeping());
                    SfmlFunctionTest.assertPlanOwnership(run.program, new IdentityHashMap<>());
                }));
        return tests;
    }

    @Test
    void nestedBreakLeavesOnlyNearestLoopAndReturnStillEscapesFunction() throws Exception {
        Run run = new Run(
            "state count=0 state answer=0 integer choose(){ for i=0 to 3 { if(i==0){return 9;} break; } return 7; } "
                + "every 100 ticks { for outer=0 to 2 { for inner=0 to 8 { count=count+1; break; } } answer=choose(); }");
        run.tick(100);
        assertEquals(3, run.value(0));
        assertEquals(9, run.value(1));
    }

    @Test
    void keywordBlocksMixedCaseAndExistingNamesRemainValid() throws Exception {
        Run run = new Run(
            "state answer=0 let break=1 void break() { return; } EVERY 100 TICKS DO "
                + "break=2; break(); FOR i=0 TO 8 DO answer=answer+break; BrEaK END END");
        run.tick(100);
        assertEquals(2, run.value(0));
    }

    @Test
    void functionBreakOutsideLexicalLoopIsRejectedEvenWhenCalledFromATriggerOrLoop() {
        for (String source : new String[] { "void bad(){break;} every 20 ticks {bad();}",
            "void bad(){break;} every 20 ticks {for i=0 to 1{bad();}}" }) {
            ProgramBuildResult result = new ProgramBuilder(source).build();
            assertFalse(result.isSuccess());
            assertTrue(
                result.getErrorMessage()
                    .contains("BREAK requires"),
                result.getErrorMessage());
        }
    }

    @TestFactory
    Collection<DynamicTest> triggerBreakEndsOnlyCurrentActivationIncludingAfterWait() {
        Collection<DynamicTest> tests = new ArrayList<>();
        for (boolean wait : new boolean[] { false, true }) for (boolean braces : new boolean[] { false, true }) {
            tests.add(DynamicTest.dynamicTest("trigger break wait=" + wait + " braces=" + braces, () -> {
                Run run = new Run(
                    "state runs=0 state after=0 state other=0 every 100 ticks " + (braces ? "{" : "do")
                        + " runs=runs+1; "
                        + (wait ? "wait(1); " : "")
                        + "if(true) "
                        + (braces ? "{break;}" : "then break end")
                        + " after=after+1; "
                        + (braces ? "}" : "end")
                        + " every 100 ticks {other=other+1;}");
                run.tick(100);
                assertEquals(1, run.value(0));
                assertEquals(0, run.value(1));
                assertEquals(1, run.value(2), "Other due triggers must still run");
                assertEquals(wait ? 1 : 0, run.sleeping());
                run.tick(101);
                assertEquals(1, run.value(0), "No immediate restart or replay after BREAK");
                assertEquals(0, run.sleeping());
                run.tick(200);
                if (wait) run.tick(201);
                assertEquals(2, run.value(0));
                assertEquals(0, run.value(1));
                assertEquals(2, run.value(2));
                assertEquals(0, run.sleeping());
            }));
        }
        return tests;
    }

    @Test
    void triggerBreakWaitsForAnotherRedstonePulse() throws Exception {
        Run run = new Run("state runs=0 every redstone pulse {runs=runs+1; break; runs=runs+100;}");
        run.tick(1, true);
        assertEquals(1, run.value(0));
        run.tick(2, false);
        assertEquals(1, run.value(0));
        run.tick(3, true);
        assertEquals(2, run.value(0));
    }

    @Test
    void triggerBreakClearsInputsWithoutUndoingTransfersOnLegacyPath() throws Exception {
        Run run = new Run(
            "every 100 ticks { input 4 from \"source\"; output 1 to \"dest\"; break; output to \"dest\"; } "
                + "every 100 ticks { output to \"dest\"; }");
        assertFalse(run.program.hasDataFeatures());
        run.fixture.source.setInventorySlotContents(0, new net.minecraft.item.ItemStack(ITEM, 10));
        run.tick(100);
        assertEquals(9, count(run.fixture.source));
        assertEquals(1, count(run.fixture.dest));
        run.tick(200);
        assertEquals(8, count(run.fixture.source));
        assertEquals(2, count(run.fixture.dest));
    }

    @Test
    void normalFailuresAreNotMistakenForLoopControl() throws Exception {
        Run run = new Run("state answer=0 every 100 ticks { while(true) { let zero=0; answer=1/zero; break; } }");
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> run.tick(100));
        assertTrue(
            failure.getMessage()
                .toLowerCase()
                .contains("zero"),
            failure.getMessage());
        assertEquals(0, run.value(0));
    }

    @Test
    void breakPreservesPriorInputRegistrationsAndResourceConservation() throws Exception {
        Run run = new Run(
            "every 100 ticks { input 4 from \"source\" "
                + "for i=0 to 5 { output 1 to \"dest\"; break; } output to \"dest\"; }");
        run.fixture.source.setInventorySlotContents(0, new net.minecraft.item.ItemStack(ITEM, 10));
        run.tick(100);
        assertEquals(6, count(run.fixture.source));
        assertEquals(4, count(run.fixture.dest));
    }

    @Test
    void visualImportRefusesBreakWithoutDroppingItsSource() {
        String source = "every 20 ticks { for i=0 to 1 { break; } print(1); }";
        BlueprintCompiler.ImportResult imported = BlueprintCompiler.inspectText(source);
        assertFalse(imported.report.isAvailable());
        assertTrue(
            imported.report.getBlockers()
                .stream()
                .anyMatch(b -> b.kind == VisualCapabilityReport.Kind.BREAK_STATEMENT));
        assertNull(imported.graph);
    }

    private static final class Run implements StateAccess {

        final Fixture fixture = new Fixture();
        final TileEntityManager manager;
        final Program program;
        final DataValue[] values;

        Run(String source) throws Exception {
            manager = RecoveryFixtures.manager(fixture);
            ProgramBuildResult result = new ProgramBuilder(source).build();
            assertTrue(result.isSuccess(), result.getErrorMessage());
            program = result.getProgram();
            values = new DataValue[program.getStateSchema()
                .size()];
            for (int i = 0; i < values.length; i++) values[i] = program.getStateSchema()
                .getDeclarations()
                .get(i)
                .getDefaultValue();
        }

        void tick(long now) {
            tick(now, false);
        }

        void tick(long now, boolean pulse) {
            fixture.world.time = now;
            ProgramContext context = new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                now,
                pulse);
            context.setStateAccess(this);
            program.tick(context);
        }

        long value(int slot) {
            return values[slot].asLong();
        }

        int sleeping() {
            return manager.getProgramExecution(program)
                .sleepingCount();
        }

        public DataValue read(int slot) {
            return values[slot];
        }

        public void write(int slot, DataValue value) {
            values[slot] = value;
        }
    }
}
