package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.block.BlockFancyCable;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.util.BlockPos;

class FullCableParityTest {

    @Test
    void fullCubeShapeAndMixedPathsKeepDiscoveryAndLoadedBoundaries() throws Exception {
        BlockCable full = new BlockCable();
        assertTrue(full.isOpaqueCube());
        assertTrue(full.renderAsNormalBlock());
        full.setBlockBoundsForItemRender();
        assertEquals(0, full.getBlockBoundsMinX());
        assertEquals(0, full.getBlockBoundsMinY());
        assertEquals(0, full.getBlockBoundsMinZ());
        assertEquals(1, full.getBlockBoundsMaxX());
        assertEquals(1, full.getBlockBoundsMaxY());
        assertEquals(1, full.getBlockBoundsMaxZ());
        for (int style = 0; style < 3; style++) {
            FakeWorld world = world();
            BlockPos first = new BlockPos(1, 64, 0), second = new BlockPos(2, 64, 0),
                inventory = new BlockPos(3, 64, 0);
            world.blocks.put(first, style == 1 ? new BlockFancyCable() : full);
            world.blocks.put(second, style == 0 ? full : new BlockFancyCable());
            world.tiles.put(inventory, new InventoryTile(1));
            CableNetwork network = new CableNetwork(world, MANAGER);
            assertTrue(network.isConnected(inventory), "style " + style);
            assertTrue(full.canConnectTo(world, second.x, second.y, second.z, ForgeDirection.EAST));
            world.unloaded.add(inventory);
            assertFalse(full.canConnectTo(world, second.x, second.y, second.z, ForgeDirection.EAST));
            network.scan();
            assertFalse(network.isConnected(inventory));
            world.unloaded.clear();
            world.blocks.remove(first);
            CableNetwork.invalidate(world, first.x, first.y, first.z);
            network.scan();
            assertFalse(network.isConnected(inventory));
        }
    }
}
