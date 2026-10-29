package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.integration.mui2.ManagerMui2Screen;

class ManagerDraftRetentionTest {

    @Test
    void ninthManagerRefusesEditingAndForcedCloseNeverEvictsAnEarlierDraft() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ManagerMui2Screen screen = allocate(ManagerMui2Screen.class);
        field("manager").set(screen, manager);
        Method retain = ManagerMui2Screen.class.getDeclaredMethod("retainDraft");
        Method capacity = ManagerMui2Screen.class.getDeclaredMethod("hasRecoveryCapacity");
        retain.setAccessible(true);
        capacity.setAccessible(true);
        Map<?, ?> worlds = (Map<?, ?>) field("DRAFTS").get(null);
        worlds.clear();
        try {
            for (int i = 0; i < 8; i++) {
                manager.xCoord = i;
                assertEquals(true, capacity.invoke(screen));
                field("pasteDraft").set(screen, "unsaved source " + i);
                retain.invoke(screen);
            }
            manager.xCoord = 8;
            assertEquals(false, capacity.invoke(screen));
            // Even a forced loss after an exceptional admission path must preserve all existing input.
            field("pasteDraft").set(screen, "forced-close fallback");
            retain.invoke(screen);
            Map<?, ?> drafts = (Map<?, ?>) worlds.get(fixture.world);
            assertEquals(9, drafts.size());
            for (int i = 0; i < 8; i++) {
                Object draft = drafts.get(new ca.teamdman.sfm.common.util.BlockPos(i, manager.yCoord, manager.zCoord));
                Field text = draft.getClass()
                    .getDeclaredField("text");
                text.setAccessible(true);
                assertEquals("unsaved source " + i, text.get(draft));
            }
            manager.xCoord = 0;
            assertEquals(true, capacity.invoke(screen));
        } finally {
            worlds.clear();
        }
    }

    private static Field field(String name) throws Exception {
        Field field = ManagerMui2Screen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
