package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.integration.mui2.ManagerInventoryLayout;

class ManagerInventoryLayoutTest {

    @Test
    void presetsUseActualCapacityAndNeverBorrowTheHotbar() {
        ManagerInventoryLayout layout = new ManagerInventoryLayout(72);
        assertEquals(48, layout.count(0));
        assertEquals(12, layout.count(1));
        assertEquals(12, layout.count(2));
        layout.preset(false);
        assertEquals(72, layout.count(0));
        assertEquals(0, layout.count(1));
        assertThrows(IllegalArgumentException.class, () -> layout.section(8));
        assertThrows(IllegalArgumentException.class, () -> layout.section(81));
        for (int capacity : new int[] { 0, 1, 27, 28, 72, 73, 216, 217 })
            checkPartition(new ManagerInventoryLayout(capacity));
    }

    @Test
    void resizeBorrowsOnlyFromMainAndRejectsGrowthAtCapacity() {
        ManagerInventoryLayout layout = new ManagerInventoryLayout(24);
        layout.preset(false);
        for (int i = 0; i < 8; i++) assertTrue(layout.resize(1, false, 1));
        assertEquals(0, layout.count(0));
        assertFalse(layout.resize(2, false, 1));
        assertFalse(layout.resize(1, true, 1));
        assertEquals(24, layout.count(1));
        assertTrue(layout.resize(1, false, -1));
        assertTrue(layout.resize(2, false, 1));
        assertEquals(21, layout.count(1));
        assertEquals(3, layout.count(2));
        assertEquals(0, layout.count(0));
        checkPartition(layout);
    }

    @Test
    void thousandsOfLayoutChangesRetainUniqueStableCanonicalIndicesAndUndoCopies() {
        Random random = new Random(1037);
        for (int capacity : new int[] { 1, 27, 72, 73, 216, 217 }) {
            ManagerInventoryLayout layout = new ManagerInventoryLayout(capacity);
            for (int step = 0; step < 500; step++) {
                ManagerInventoryLayout previous = layout.copy();
                String before = previous.summary();
                int section = random.nextBoolean() ? 1 : 2;
                int opposite = layout.count(section == 1 ? 2 : 1);
                layout.resize(section, random.nextBoolean(), random.nextBoolean() ? 1 : -1);
                assertEquals(opposite, layout.count(section == 1 ? 2 : 1));
                for (int canonical = 9; canonical < capacity + 9; canonical++) {
                    int untouched = section == 1 ? 2 : 1;
                    assertEquals(
                        previous.section(canonical) == untouched,
                        layout.section(canonical) == untouched,
                        "Resizing one breakout must not borrow or replace the other breakout's indices");
                }
                assertEquals(before, previous.summary(), "Undo snapshots must remain independent");
                checkPartition(layout);
                checkPartition(previous);
            }
        }
    }

    private static void checkPartition(ManagerInventoryLayout layout) {
        assertEquals(layout.storage, layout.count(0) + layout.count(1) + layout.count(2));
        Set<String> positions = new HashSet<>();
        int[] counts = new int[3];
        for (int canonical = 9; canonical < layout.storage + 9; canonical++) {
            int section = layout.section(canonical);
            counts[section]++;
            int offset = layout.offset(canonical);
            assertTrue(offset >= 0 && offset < layout.count(section));
            assertTrue(positions.add(section + ":" + offset), "A canonical index must occupy exactly one cell");
        }
        assertArrayEquals(new int[] { layout.count(0), layout.count(1), layout.count(2) }, counts);
        assertEquals(layout.storage, positions.size());
    }
}
