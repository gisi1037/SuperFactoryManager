package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import ca.teamdman.sfm.common.diagnostics.ManagerPhaseProfiler;

@EnabledIfSystemProperty(named = "sfm.phaseProfile", matches = "false")
class ManagerPhaseProfilerDisabledTest {

    @Test
    void ordinaryRuntimeDoesNotCollectPhaseData() {
        assertFalse(ManagerPhaseProfiler.ENABLED);
        ManagerPhaseProfiler.reset();
        ManagerPhaseProfiler.scan(0, 100, 500);
        ManagerPhaseProfiler.compatibility(0);
        ManagerPhaseProfiler.program(0);
        ManagerPhaseProfiler.update(12345);
        assertEquals(0, ManagerPhaseProfiler.begin());
        assertArrayEquals(new long[8], ManagerPhaseProfiler.snapshot());
    }
}
