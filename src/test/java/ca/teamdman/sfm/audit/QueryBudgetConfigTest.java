package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.teamdman.sfm.Config;
import cpw.mods.fml.relauncher.FMLInjectionData;

class QueryBudgetConfigTest {

    @TempDir
    Path directory;

    @Test
    void forgeConfigDefaultsToFourAndClampsOutOfRangeSettings() throws Exception {
        int previous = Config.queryBudgetMultiplier;
        int copierTicks = Config.diskCopierTicksPerCycle, copierBatch = Config.diskCopierCopiesPerCycle;
        String greeting = Config.greeting;
        boolean export = Config.exportLogsToFile, discord = Config.showDiscordButton;
        Field home = FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        Object previousHome = home.get(null);
        try {
            // Forge normally receives its game directory from LaunchWrapper; use only the test directory here.
            home.set(null, directory.toFile());
            Path file = directory.resolve("superfactorymanager.cfg");
            Config.synchronizeConfiguration(file.toFile());
            assertEquals(4, Config.queryBudgetMultiplier);
            for (int[] sample : new int[][] { { 1, 1 }, { 8, 8 }, { 16, 16 }, { 0, 1 }, { 999, 16 } }) {
                Files.write(
                    file,
                    ("performance {\n I:queryBudgetMultiplier=" + sample[0] + "\n}\n")
                        .getBytes(StandardCharsets.UTF_8));
                Config.synchronizeConfiguration(file.toFile());
                assertEquals(sample[1], Config.queryBudgetMultiplier);
            }
        } finally {
            home.set(null, previousHome);
            Config.queryBudgetMultiplier = previous;
            Config.diskCopierTicksPerCycle = copierTicks;
            Config.diskCopierCopiesPerCycle = copierBatch;
            Config.greeting = greeting;
            Config.exportLogsToFile = export;
            Config.showDiscordButton = discord;
        }
    }
}
