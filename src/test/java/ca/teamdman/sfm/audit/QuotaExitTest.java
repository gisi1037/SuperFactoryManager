package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

/** A satisfied aggregate quota must not walk the remaining destination entries. */
class QuotaExitTest {

    private static final class Source extends InventoryTile {

        int reads;

        Source() {
            super(1);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            reads++;
            return super.getStackInSlot(slot);
        }
    }

    @Test
    void quantityExhaustionLeavesNoDestinationLengthReadTail() throws Exception {
        Fixture f = new Fixture();
        Source source = new Source();
        InventoryTile destination = new InventoryTile(1000);
        source.setInventorySlotContents(0, new ItemStack(ITEM, 20));
        f.world.tiles.put(SRC, source);
        f.world.tiles.put(DST, destination);
        f.run("INPUT FROM source OUTPUT 1 TO dest");
        assertTrue(source.reads < 10, "Quota exhaustion must bound reads independently of destination length");
        assertEquals(19, count(source));
        assertEquals(1, count(destination));
        assertSame(
            ITEM,
            destination.getStackInSlot(0)
                .getItem());
    }

    @Test
    void retentionCapExhaustsAfterFillingTheExistingStack() throws Exception {
        Fixture f = new Fixture();
        Source source = new Source();
        InventoryTile destination = new InventoryTile(1000);
        source.setInventorySlotContents(0, new ItemStack(ITEM, 20));
        destination.setInventorySlotContents(0, new ItemStack(ITEM, 60));
        f.world.tiles.put(SRC, source);
        f.world.tiles.put(DST, destination);
        f.run("INPUT FROM source OUTPUT RETAIN 64 TO dest");
        assertTrue(source.reads < 10);
        assertEquals(16, count(source));
        assertEquals(64, destination.getStackInSlot(0).stackSize);
        assertNull(destination.getStackInSlot(1));
    }

    @Test
    void rejectedAndPartialSlotsAreVisitedUntilTheQuotaReallyFills() throws Exception {
        Fixture f = new Fixture();
        Source source = new Source();
        InventoryTile destination = new InventoryTile(1000);
        source.setInventorySlotContents(0, new ItemStack(ITEM, 20));
        destination.setInventorySlotContents(0, new ItemStack(OTHER, 64));
        destination.setInventorySlotContents(1, new ItemStack(ITEM, 63));
        destination.setInventorySlotContents(2, new ItemStack(ITEM, 63));
        f.world.tiles.put(SRC, source);
        f.world.tiles.put(DST, destination);
        f.run("INPUT FROM source OUTPUT 3 TO dest");
        assertTrue(source.reads < 15);
        assertEquals(17, count(source));
        assertSame(
            OTHER,
            destination.getStackInSlot(0)
                .getItem());
        assertEquals(64, destination.getStackInSlot(1).stackSize);
        assertEquals(64, destination.getStackInSlot(2).stackSize);
        assertEquals(1, destination.getStackInSlot(3).stackSize);
        assertNull(destination.getStackInSlot(4));
    }
}
