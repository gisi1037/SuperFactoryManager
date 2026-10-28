package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import ca.teamdman.sfm.common.diagnostics.ManagerPhaseProfiler;

@EnabledIfSystemProperty(named = "sfm.phaseProfile", matches = "true")
class ManagerPhaseProfilerTest {

    @Test
    void countersResetAndSnapshotsDoNotAliasMutableStorage() {
        ManagerPhaseProfiler.reset();
        ManagerPhaseProfiler.scan(ManagerPhaseProfiler.begin(), 16, 82);
        ManagerPhaseProfiler.compatibility(ManagerPhaseProfiler.begin());
        ManagerPhaseProfiler.program(ManagerPhaseProfiler.begin());
        ManagerPhaseProfiler.update(1234);
        long[] first = ManagerPhaseProfiler.snapshot();
        assertEquals(1, first[1]);
        assertEquals(16, first[2]);
        assertEquals(82, first[3]);
        assertEquals(1234, first[6]);
        assertEquals(1, first[7]);
        ManagerPhaseProfiler.reset();
        assertArrayEquals(new long[8], ManagerPhaseProfiler.snapshot());
        assertEquals(16, first[2]);
    }
}
