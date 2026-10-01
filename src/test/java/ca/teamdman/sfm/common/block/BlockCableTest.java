package ca.teamdman.sfm.common.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class BlockCableTest {

    @Test
    public void testFancyCableProperties() {
        BlockFancyCable fancy = new BlockFancyCable();
        assertTrue(fancy instanceof BlockCable);
        assertFalse(fancy.isOpaqueCube());
        assertFalse(fancy.renderAsNormalBlock());

        BlockFancyCable.renderId = 42;
        assertEquals(42, fancy.getRenderType());

        fancy.setBlockBoundsForItemRender();
        assertEquals(0.25F, (float) fancy.getBlockBoundsMinX(), 0.0001F);
        assertEquals(0.25F, (float) fancy.getBlockBoundsMinY(), 0.0001F);
        assertEquals(0.25F, (float) fancy.getBlockBoundsMinZ(), 0.0001F);
        assertEquals(0.75F, (float) fancy.getBlockBoundsMaxX(), 0.0001F);
        assertEquals(0.75F, (float) fancy.getBlockBoundsMaxY(), 0.0001F);
        assertEquals(0.75F, (float) fancy.getBlockBoundsMaxZ(), 0.0001F);
    }

    @Test
    public void testCableHierarchy() {
        BlockCable cable = new BlockCable();
        BlockFancyCable fancy = new BlockFancyCable();

        assertTrue(cable instanceof BlockCable);
        assertTrue(fancy instanceof BlockCable);
    }
}
