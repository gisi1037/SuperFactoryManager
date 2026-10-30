package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.teamdman.sfm.client.gui.mui2.StudioScalePreference;

class StudioScaleTest {

    @TempDir
    Path directory;

    @Test
    void followsGameByDefaultButIndependentChoiceSurvivesRestartAndSmallWindow() {
        Path path = directory.resolve("studio.properties");
        StudioScalePreference settings = new StudioScalePreference(path);
        assertEquals(0, settings.preferred());
        assertEquals(3, settings.effective(1920, 1080, 3));
        settings.remember(2);
        settings = new StudioScalePreference(path);
        assertEquals(2, settings.effective(1920, 1080, 3));
        assertEquals(2, settings.effective(1920, 1080, 1));
        assertEquals(1, settings.effective(640, 360, 1));
        assertEquals(2, settings.preferred(), "Fitting a small window must not overwrite the chosen scale");
        settings.remember(0);
        assertEquals(3, new StudioScalePreference(path).effective(1920, 1080, 3));
    }

    @Test
    void customScalesFitTheMinimumWorkspaceAndTransformCoversEveryScreenEdge() {
        StudioScalePreference settings = new StudioScalePreference(directory.resolve("studio.properties"));
        for (int[] size : new int[][] { { 640, 480 }, { 854, 480 }, { 1920, 1080 }, { 2557, 1352 }, { 3840, 2160 } }) {
            for (int requested = 1; requested <= StudioScalePreference.MAX_SCALE; requested++) {
                settings.remember(requested);
                int effective = settings.effective(size[0], size[1], 3);
                assertTrue(size[0] / effective >= 320);
                assertTrue(size[1] / effective >= 240);
                for (int game = 1; game <= StudioScalePreference.maximum(size[0], size[1]); game++) {
                    float ratio = effective / (float) game;
                    for (int pixels : size) {
                        int gameSize = (pixels + game - 1) / game;
                        int logical = StudioScalePreference.logicalSize(gameSize, ratio);
                        assertTrue(logical * (double) ratio >= gameSize);
                        assertTrue((logical - 1) * (double) ratio < gameSize);
                    }
                }
            }
        }
    }

    @Test
    void corruptAndOversizedPreferencesFallBackWithoutBlockingTheEditor() throws Exception {
        Path path = directory.resolve("studio.properties");
        for (String value : new String[] { "-1", "17", "not-a-scale", "\\uNOPE" }) {
            Files.write(path, ("guiScale=" + value).getBytes(StandardCharsets.UTF_8));
            assertEquals(0, new StudioScalePreference(path).preferred());
        }
        Files.write(path, new byte[4097]);
        assertEquals(0, new StudioScalePreference(path).preferred());
        assertThrows(IllegalArgumentException.class, () -> new StudioScalePreference(path).remember(-1));
    }
}
