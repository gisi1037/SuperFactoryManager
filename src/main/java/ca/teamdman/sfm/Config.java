package ca.teamdman.sfm;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Super Factory Manager initialized";
    public static boolean exportLogsToFile = false;
    public static boolean showDiscordButton = true;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "Greeting");
        exportLogsToFile = configuration.getBoolean(
            "exportLogsToFile",
            Configuration.CATEGORY_GENERAL,
            false,
            "Export manager execution logs to logs/sfm_server_export.log (disabled by default)");
        showDiscordButton = configuration.getBoolean(
            "showDiscordButton",
            Configuration.CATEGORY_GENERAL,
            true,
            "Show the Discord button on the Factory Manager GUI");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
