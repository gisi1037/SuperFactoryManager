package ca.teamdman.sfm;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Super Factory Manager initialized";

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "Greeting");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
