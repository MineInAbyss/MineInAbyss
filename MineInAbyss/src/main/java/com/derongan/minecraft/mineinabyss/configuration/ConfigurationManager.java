package com.derongan.minecraft.mineinabyss.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigurationManager {
    private FileConfiguration configuration;

    public ConfigurationManager(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    public static void createConfig(Plugin plugin) {
        try {
            if (!plugin.getDataFolder().exists()) {
                if(!plugin.getDataFolder().mkdirs()){
                    throw new RuntimeException("Failed to make config file");
                }
            }
            File file = new File(plugin.getDataFolder(), "config.yml");
            if (!file.exists()) {
                plugin.getLogger().info("Config.yml not found, creating!");
                plugin.saveDefaultConfig();
            } else {
                plugin.getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
