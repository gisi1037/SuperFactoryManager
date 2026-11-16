package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.block.BlockFancyCable;
import ca.teamdman.sfm.common.util.BlockPos;

class FancyCableShapeTest {

    private static final BlockPos POSITION = new BlockPos(15, 64, -17);

    @Test
    void allConnectionShapesKeepTheirGapsEmptyForTargetingAndEntityCollision() throws Exception {
        BlockFancyCable cable = new BlockFancyCable();
        for (int mask = 0; mask < 64; mask++) {
            FakeWorld world = connected(mask);
            assertNull(trace(cable, world, .9, .9, -1, .9, .9, 2), "Empty upper corner for mask " + mask);
            List<AxisAlignedBB> boxes = new ArrayList<>();
            cable.addCollisionBoxesToList(
                world,
                POSITION.x,
                POSITION.y,
                POSITION.z,
                box(.85, .85, .2, .95, .95, .8),
                boxes,
                null);
            assertTrue(boxes.isEmpty(), "Entity collision must leave the same corner empty");
            cable.addCollisionBoxesToList(
                world,
                POSITION.x,
                POSITION.y,
                POSITION.z,
                box(-1, -1, -1, 2, 2, 2),
                boxes,
                null);
            assertEquals(1 + Integer.bitCount(mask), boxes.size());
        }
    }

    @Test
    void raysHitTheClosestVisibleFaceAndReturnTheCorrectBlockAndPlacementSide() throws Exception {
        BlockFancyCable cable = new BlockFancyCable();
        for (int mask = 0; mask < 64; mask++) for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            FakeWorld world = connected(mask);
            MovingObjectPosition hit = trace(
                cable,
                world,
                .5 + side.offsetX * 2,
                .5 + side.offsetY * 2,
                .5 + side.offsetZ * 2,
                .5,
                .5,
                .5);
            assertNotNull(hit);
            assertEquals(POSITION.x, hit.blockX);
            assertEquals(POSITION.y, hit.blockY);
            assertEquals(POSITION.z, hit.blockZ);
            assertEquals(side.ordinal(), hit.sideHit);
            boolean arm = (mask & 1 << side.ordinal()) != 0;
            assertEquals(arm ? side.ordinal() + 1 : 0, hit.subHit);
            double radius = arm ? .5 : .25;
            assertEquals(POSITION.x + .5 + side.offsetX * radius, hit.hitVec.xCoord, 1e-9);
            assertEquals(POSITION.y + .5 + side.offsetY * radius, hit.hitVec.yCoord, 1e-9);
            assertEquals(POSITION.z + .5 + side.offsetZ * radius, hit.hitVec.zCoord, 1e-9);
        }
    }

    @Test
    void theNarrowArmAndWiderCoreBothRemainTargetable() throws Exception {
        BlockFancyCable cable = new BlockFancyCable();
        FakeWorld world = connected(1 << ForgeDirection.EAST.ordinal());
        MovingObjectPosition arm = trace(cable, world, .9, .5, -1, .9, .5, 2);
        assertNotNull(arm);
        assertEquals(ForgeDirection.EAST.ordinal() + 1, arm.subHit);
        assertEquals(POSITION.z + 5.0 / 16, arm.hitVec.zCoord, 1e-9);
        assertNotNull(trace(cable, world, .7, .7, -1, .7, .7, 2), "Core extends beyond the arm's cross-section");
        assertNull(trace(cable, world, .9, .7, -1, .9, .7, 2), "Empty space beside the narrow arm");
        world.blocks.clear();
        assertNull(trace(cable, world, .9, .5, -1, .9, .5, 2), "Disconnected arms have no hitbox");
    }

    @Test
    void rayTracingCannotReuseOtherBlocksBoundsOrReadUnloadedNeighbors() throws Exception {
        BlockFancyCable cable = new BlockFancyCable();
        FakeWorld world = connected(63);
        cable.setBlockBoundsBasedOnState(world, POSITION.x, POSITION.y, POSITION.z);
        world.blocks.clear();
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) world.unloaded.add(POSITION.offset(side));
        assertNull(trace(cable, world, .9, .9, -1, .9, .9, 2));
        MovingObjectPosition hit = trace(cable, world, 2, .5, .5, .5, .5, .5);
        assertNotNull(hit);
        assertEquals(POSITION.x + .75, hit.hitVec.xCoord, 1e-9);
    }

    private static FakeWorld connected(int mask) throws Exception {
        FakeWorld world = world();
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
            if ((mask & 1 << side.ordinal()) != 0) world.blocks.put(POSITION.offset(side), new BlockCable());
        return world;
    }

    private static MovingObjectPosition trace(BlockFancyCable cable, FakeWorld world, double x, double y, double z,
        double endX, double endY, double endZ) {
        return cable.collisionRayTrace(
            world,
            POSITION.x,
            POSITION.y,
            POSITION.z,
            Vec3.createVectorHelper(POSITION.x + x, POSITION.y + y, POSITION.z + z),
            Vec3.createVectorHelper(POSITION.x + endX, POSITION.y + endY, POSITION.z + endZ));
    }

    private static AxisAlignedBB box(double x, double y, double z, double endX, double endY, double endZ) {
        return AxisAlignedBB.getBoundingBox(
            POSITION.x + x,
            POSITION.y + y,
            POSITION.z + z,
            POSITION.x + endX,
            POSITION.y + endY,
            POSITION.z + endZ);
    }
}
