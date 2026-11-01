package ca.teamdman.sfm;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Super Factory Manager initialized";
    public static boolean exportLogsToFile = false;
    public static boolean showDiscordButton = true;
    public static int queryBudgetMultiplier = 4;

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
        queryBudgetMultiplier = configuration.getInt(
            "queryBudgetMultiplier",
            "performance",
            4,
            1,
            ca.teamdman.sfml.data.QueryBudget.MAX_MULTIPLIER,
            "Server-side inventory query/transfer work allowance per manager update. "
                + "1 restores original limits; 4 allows 65536 slot-work units. Range 1-16. "
                + "Scales position, provider-call, slot-work and allocation limits together. "
                + "Higher values allow larger batches but can increase server tick time. Restart to apply.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
