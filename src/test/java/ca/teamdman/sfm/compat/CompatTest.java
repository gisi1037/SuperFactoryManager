package ca.teamdman.sfm.compat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.compat.stevefm.SteveFMCompat;
import ca.teamdman.sfm.compat.stevesaddons.StevesAddonsCompat;

public class CompatTest {

    @Test
    public void testCompatDetectionSafety() {
        assertDoesNotThrow(() -> {
            Compat.isSFMLoaded();
            Compat.isStevesAddonsLoaded();
            Compat.isNEILoaded();
        });
    }

    @Test
    public void testSteveFMCompatNullSafety() {
        assertFalse(SteveFMCompat.isCable(null, 0));
    }

    @Test
    public void testStevesAddonsCompatNullSafety() {
        assertNull(StevesAddonsCompat.getSavedName(null, 0, 0, 0));
        assertDoesNotThrow(() -> {
            StevesAddonsCompat.onBlockTagged(null, 0, 0, 0, "test");
            StevesAddonsCompat.onBlockUntagged(null, 0, 0, 0);
            StevesAddonsCompat.syncAll(null, null);
        });
    }
}
