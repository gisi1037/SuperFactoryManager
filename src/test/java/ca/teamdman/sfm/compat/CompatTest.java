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
        assertNull(SteveFMCompat.getInventory(null, null, null));
        assertNull(SteveFMCompat.getFluidHandler(null, null, null));
        assertFalse(SteveFMCompat.isReceiver(null, null));
        org.junit.jupiter.api.Assertions.assertEquals(0, SteveFMCompat.getReceiverPower(null, null));
        org.junit.jupiter.api.Assertions.assertEquals(0, SteveFMCompat.getMaxReceiverPower(null, null));
        assertFalse(SteveFMCompat.isEmitter(null, null));
        assertFalse(SteveFMCompat.setRedstoneOutput(null, null, null, 15, true));
        assertFalse(SteveFMCompat.isBUD(null, null));
        assertFalse(SteveFMCompat.checkAndClearBUDUpdates(null, null));
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
