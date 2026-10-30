package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.tileentity.LogEntry;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.integration.mui2.ManagerLogView;
import ca.teamdman.sfm.integration.mui2.ManagerMachineLayout;

class ManagerMachineConsoleTest {

    @Test
    void normalMachineFootprintKeepsStockInventoryAndUsefulContentAtEveryGuiScale() {
        for (int height : new int[] { 240, 256, 360, 480, 720, 1080 }) {
            int machine = ManagerMachineLayout.height(height);
            assertTrue(machine <= height - 8);
            assertTrue(machine <= 288);
            assertTrue(ManagerMachineLayout.contentHeight(height) >= 70);
            assertTrue(ManagerMachineLayout.CONTENT_TOP + ManagerMachineLayout.contentHeight(height) < machine - 82);
            assertTrue(machine - 6 - 76 > ManagerMachineLayout.CONTENT_TOP);
        }
        assertEquals(312, ManagerMachineLayout.WIDTH);
        assertEquals(288, ManagerMachineLayout.height(1080));
    }

    @Test
    void logSearchAndSeverityAreIndependentOfRecordingAndKeepChronologicalOrder() {
        List<LogEntry> entries = Arrays.asList(
            new LogEntry(0, LogLevel.ERROR, "Tank failed"),
            new LogEntry(1000, LogLevel.INFO, "Tank ready"),
            new LogEntry(2000, LogLevel.WARN, "Chest full"));
        assertEquals(
            Arrays.asList("00:00:00 [ERROR] Tank failed"),
            ManagerLogView.filter(entries, LogLevel.WARN, "TANK"));
        assertEquals(
            2,
            ManagerLogView.filter(entries, null, "tank")
                .size());
        assertEquals(
            3,
            ManagerLogView.filter(entries, null, "")
                .size());
        assertEquals(
            "No matching retained logs.",
            ManagerLogView.filter(entries, LogLevel.ERROR, "ready")
                .get(0));
        assertEquals(3, entries.size());
        assertEquals(
            LogLevel.INFO,
            entries.get(1)
                .getLevel());
    }

    @Test
    void logViewIsBoundedAndDoesNotAlterEntryPayload() {
        List<LogEntry> entries = new ArrayList<>();
        for (int i = 0; i < 500; i++) entries.add(new LogEntry(i * 1000L, LogLevel.DEBUG, "entry " + i));
        List<String> displayed = ManagerLogView.filter(entries, null, "");
        assertEquals(200, displayed.size());
        assertTrue(
            displayed.get(0)
                .endsWith("entry 300"));
        assertTrue(
            displayed.get(199)
                .endsWith("entry 499"));
        assertEquals(
            "entry 300",
            entries.get(300)
                .getMessage());
    }
}
