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

class DiskCopierConfigTest {

    @TempDir
    Path directory;

    @Test
    void forgeConfigDefaultsToOneSecondEightCopiesAndClampsBothLimits() throws Exception {
        int ticks = Config.diskCopierTicksPerCycle, batch = Config.diskCopierCopiesPerCycle,
            query = Config.queryBudgetMultiplier;
        String greeting = Config.greeting;
        boolean logs = Config.exportLogsToFile, discord = Config.showDiscordButton;
        Field home = FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        Object previous = home.get(null);
        try {
            home.set(null, directory.toFile());
            Path config = directory.resolve("superfactorymanager.cfg");
            Config.synchronizeConfiguration(config.toFile());
            assertEquals(20, Config.diskCopierTicksPerCycle);
            assertEquals(8, Config.diskCopierCopiesPerCycle);
            for (int[] row : new int[][] { { 1, 1, 1, 1 }, { 40, 4, 40, 4 }, { 0, 0, 1, 1 },
                { 99999, 99, 12000, 8 } }) {
                Files.write(
                    config,
                    ("machines {\n I:diskCopierTicksPerCycle=" + row[0]
                        + "\n I:diskCopierCopiesPerCycle="
                        + row[1]
                        + "\n}\n").getBytes(StandardCharsets.UTF_8));
                Config.synchronizeConfiguration(config.toFile());
                assertEquals(row[2], Config.diskCopierTicksPerCycle);
                assertEquals(row[3], Config.diskCopierCopiesPerCycle);
            }
        } finally {
            home.set(null, previous);
            Config.diskCopierTicksPerCycle = ticks;
            Config.diskCopierCopiesPerCycle = batch;
            Config.queryBudgetMultiplier = query;
            Config.greeting = greeting;
            Config.exportLogsToFile = logs;
            Config.showDiscordButton = discord;
        }
    }
}
