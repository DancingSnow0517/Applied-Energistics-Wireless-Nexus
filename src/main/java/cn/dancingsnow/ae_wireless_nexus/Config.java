package cn.dancingsnow.ae_wireless_nexus;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public final class Config {

    private Config() {}

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
