package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.BoolExpr;
import ca.teamdman.sfml.ast.IfStatement;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class LabelledRedstoneTest {

    @Test
    void variableLabelAndSelectedFaceControlTheBranch() throws Exception {
        Fixture fixture = poweredFixture();
        fixture.stock(0, 2);
        fixture.runProgram("""
            every 20 ticks {
              let sensor = label("source").side("north")
              let threshold = 8
              if sensor redstone >= (threshold) {
                input 1 from "source"
                output to "dest"
              }
            }
            """);
        assertEquals(1, count(fixture.dest));
        assertTrue(condition("\"source\" NORTH SIDE REDSTONE >= 8").evaluate(context(fixture)));
        assertFalse(condition("\"source\" TOP SIDE REDSTONE > 0").evaluate(context(fixture)));
        assertTrue(condition("\"source\" REDSTONE >= 8").evaluate(context(fixture)));
    }

    @Test
    void unloadedNeighborsAndDisconnectedTargetsCannotSupplyASignal() throws Exception {
        Fixture fixture = poweredFixture();
        BlockPos neighbor = SRC.offset(ForgeDirection.UP);
        fixture.world.unloaded.add(neighbor);
        assertUnavailable("\"source\" TOP SIDE REDSTONE = 0", fixture);
        assertTrue(condition("\"source\" NORTH SIDE REDSTONE >= 8").evaluate(context(fixture)));

        BlockPos remote = new BlockPos(3200, 64, 3200);
        fixture.labels.addPosition("remote", remote);
        assertUnavailable("\"remote\" REDSTONE = 0", fixture);
        fixture.world.unloaded.add(remote);
        assertUnavailable("\"remote\" REDSTONE = 0", fixture);
    }

    @Test
    void labelExpansionUsesTheSharedPositionBudget() throws Exception {
        Fixture fixture = poweredFixture();
        ProgramContext context = context(fixture);
        QueryBudget budget = context.getInvocationQueryBudget();
        budget.chargePositions(budget.getPositionLimit());
        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class,
            () -> condition("\"source\" REDSTONE > 0").evaluate(context));
        assertTrue(
            failure.getMessage()
                .contains("position budget"),
            failure.getMessage());
    }

    @Test
    void rejectsItemSlotClauses() {
        assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO IF \"source\" SLOTS 0 REDSTONE > 0 THEN FORGET END END").build()
                .isSuccess());
    }

    private static Fixture poweredFixture() throws Exception {
        Fixture fixture = new Fixture();
        Block unpowered = new Block(Material.air) {};
        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS)
            fixture.world.blocks.put(SRC.offset(face), unpowered);
        fixture.world.blocks.put(SRC.offset(ForgeDirection.NORTH), new Block(Material.circuits) {

            @Override
            public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
                return side == ForgeDirection.NORTH.ordinal() ? 12 : 0;
            }
        });
        return fixture;
    }

    private static void assertUnavailable(String source, Fixture fixture) {
        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class,
            () -> condition(source).evaluate(context(fixture)));
        assertTrue(
            failure.getMessage()
                .contains("unavailable"),
            failure.getMessage());
    }

    private static ProgramContext context(Fixture fixture) {
        return new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
    }

    private static BoolExpr condition(String source) {
        return ((IfStatement) parse("EVERY 20 TICKS DO IF " + source + " THEN FORGET END END").getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0)).getCondition();
    }
}
