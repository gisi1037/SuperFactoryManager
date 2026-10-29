package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockFancyCable;
import ca.teamdman.sfm.common.inventory.InventoryUtils;
import ca.teamdman.sfm.common.util.BlockPos;

class LoadedInventoryBoundaryTest {

    @Test
    void coldAndWarmChestViewsRefuseAnUnloadedNeighborWithoutVanillaAdjacency() throws Exception {
        FakeWorld world = world();
        BlockPos leftPos = new BlockPos(15, 64, 0), rightPos = leftPos.offset(ForgeDirection.EAST);
        SafeChest left = chest(world, leftPos, 10);
        world.unloaded.add(rightPos);
        assertNull(InventoryUtils.getInventory(world, leftPos));
        assertNull(InventoryUtils.getAdjacentChest(left));
        world.unloaded.clear();
        SafeChest right = chest(world, rightPos, 20);
        assertEquals(
            54,
            InventoryUtils.getInventory(world, leftPos)
                .getSizeInventory());
        left.adjacentChestXPos = right;
        left.adjacentChestChecked = true;
        right.adjacentChestXNeg = left;
        right.adjacentChestChecked = true;
        world.unloaded.add(rightPos);
        assertNull(InventoryUtils.getInventory(world, leftPos));
        assertNull(InventoryUtils.getAdjacentChest(left));
        assertEquals(10, left.getStackInSlot(0).stackSize);
        assertEquals(20, right.getStackInSlot(0).stackSize);
    }

    @Test
    void stalePartnersAreIgnoredAndAmbiguousOrIncompletePairsAreUnavailable() throws Exception {
        FakeWorld world = world();
        BlockPos pos = new BlockPos(0, 64, 0), east = pos.offset(ForgeDirection.EAST);
        SafeChest first = chest(world, pos, 10), stale = chest(world, east, 20);
        first.adjacentChestChecked = true;
        first.adjacentChestXPos = stale;
        world.tiles.remove(east);
        assertSame(first, InventoryUtils.getInventory(world, pos));
        assertNull(InventoryUtils.getAdjacentChest(first));
        SafeChest replacement = chest(world, east, 30);
        assertSame(replacement, InventoryUtils.getAdjacentChest(first));
        assertEquals(
            30,
            InventoryUtils.getInventory(world, pos)
                .getStackInSlot(27).stackSize);
        world.unloaded.add(east.offset(ForgeDirection.EAST));
        assertNull(InventoryUtils.getInventory(world, pos));
        world.unloaded.clear();
        chest(world, east.offset(ForgeDirection.EAST), 40);
        assertNull(InventoryUtils.getInventory(world, pos));
        assertNull(InventoryUtils.getInventory(world, east));
    }

    @Test
    void oppositeHalfViewsHaveCanonicalPhysicalSlotIdentityInBothOrientations() throws Exception {
        for (ForgeDirection direction : new ForgeDirection[] { ForgeDirection.EAST, ForgeDirection.SOUTH }) {
            FakeWorld world = world();
            BlockPos firstPos = new BlockPos(0, 64, 0), secondPos = firstPos.offset(direction);
            SafeChest first = chest(world, firstPos, 10), second = chest(world, secondPos, 20);
            IInventory firstView = InventoryUtils.getInventory(world, firstPos);
            IInventory secondView = InventoryUtils.getInventory(world, secondPos);
            assertNotSame(firstView, secondView);
            assertEquals(10, secondView.getStackInSlot(0).stackSize);
            assertEquals(20, firstView.getStackInSlot(27).stackSize);
            assertTrue(InventoryUtils.samePhysicalSlot(firstView, 0, secondView, 0));
            assertTrue(InventoryUtils.samePhysicalSlot(secondView, 27, firstView, 27));
            assertTrue(InventoryUtils.samePhysicalSlot(firstView, 0, first, 0));
            assertTrue(InventoryUtils.samePhysicalSlot(second, 0, secondView, 27));
            assertFalse(InventoryUtils.samePhysicalSlot(firstView, 0, secondView, 27));
            assertFalse(InventoryUtils.samePhysicalSlot(firstView, 0, secondView, 1));
            assertFalse(InventoryUtils.samePhysicalSlot(firstView, -1, secondView, -1));
        }
    }

    @Test
    void transferAndHasSkipIncompleteChestWithoutReadingOrMutatingItsContents() throws Exception {
        Fixture fixture = new Fixture();
        SafeChest source = chest(fixture.world, SRC, 10);
        fixture.world.unloaded.add(SRC.offset(ForgeDirection.EAST));
        fixture.run("INPUT FROM source OUTPUT TO dest");
        fixture.run("IF source HAS >= 1 THEN INPUT FROM source OUTPUT TO dest END");
        assertEquals(0, count(fixture.dest));
        assertEquals(10, source.getStackInSlot(0).stackSize);
    }

    @Test
    void fancyCableBoundsAndCollisionNeverReadAnUnloadedNeighbor() throws Exception {
        FakeWorld world = world();
        BlockPos pos = new BlockPos(15, 64, 0), east = pos.offset(ForgeDirection.EAST);
        BlockFancyCable cable = new BlockFancyCable();
        world.unloaded.add(east);
        assertFalse(cable.canConnectTo(world, pos.x, pos.y, pos.z, ForgeDirection.EAST));
        assertDoesNotThrow(() -> cable.setBlockBoundsBasedOnState(world, pos.x, pos.y, pos.z));
        assertEquals(.75F, (float) cable.getBlockBoundsMaxX(), .0001F);
        assertDoesNotThrow(
            () -> cable.addCollisionBoxesToList(
                world,
                pos.x,
                pos.y,
                pos.z,
                AxisAlignedBB.getBoundingBox(14, 63, -1, 17, 66, 2),
                new ArrayList<AxisAlignedBB>(),
                null));
        world.unloaded.clear();
        world.blocks.put(east, cable);
        assertTrue(cable.canConnectTo(world, pos.x, pos.y, pos.z, ForgeDirection.EAST));
    }

    @Test
    void oppositeHalfNoOpPreservesQuotaForTheFollowingRealDestination() throws Exception {
        for (boolean reverse : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            BlockPos firstPos = new BlockPos(0, 64, -1), secondPos = new BlockPos(1, 64, -1);
            SafeChest first = chest(fixture.world, firstPos, 10);
            chest(fixture.world, secondPos, 0);
            fixture.world.blocks.put(SRC, new ca.teamdman.sfm.common.block.BlockCable());
            fixture.labels.removePosition("source", SRC);
            fixture.labels.addPosition("source", reverse ? secondPos : firstPos);
            fixture.labels.addPosition("alias", reverse ? firstPos : secondPos);
            fixture.run("INPUT 1 FROM source SLOTS 0 OUTPUT 1 TO alias SLOTS 0 OUTPUT TO dest");
            assertEquals(1, count(fixture.dest));
            assertEquals(9, first.getStackInSlot(0).stackSize);
        }
    }

    private static SafeChest chest(FakeWorld world, BlockPos pos, int amount) {
        SafeChest chest = new SafeChest();
        chest.xCoord = pos.x;
        chest.yCoord = pos.y;
        chest.zCoord = pos.z;
        chest.setWorldObj(world);
        chest.setInventorySlotContents(0, new ItemStack(ITEM, amount));
        world.tiles.put(pos, chest);
        return chest;
    }

    private static final class SafeChest extends TileEntityChest {

        @Override
        public void checkForAdjacentChests() {
            throw new AssertionError("Transfer helpers must not call unsafe vanilla adjacency discovery");
        }

        @Override
        public void markDirty() {}
    }
}
