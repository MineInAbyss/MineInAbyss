package com.derongan.minecraft.mineinabyss.configuration;

import com.derongan.minecraft.mineinabyss.MineInAbyss;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigurationManager {
    private MineInAbyss plugin;
    private File spawnLocFile;
    private FileConfiguration spawnLocConfig;

    public ConfigurationManager(MineInAbyss plugin) {
        this.plugin = plugin;
        spawnLocFile = new File(plugin.getDataFolder(), "spawn-locs.yml");

        reload();
    }

    public FileConfiguration getSpawnLocConfig() {
        return spawnLocConfig;
    }

    public void reload() {
        createConfig();
        spawnLocConfig = registerConfig(spawnLocFile);
    }

    public void createConfig() {
        try {
            if (!plugin.getDataFolder().exists()) {
                if (!plugin.getDataFolder().mkdirs()) {
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

    public void saveSpawnLocConfig() {
        try {
            spawnLocConfig.save(spawnLocFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration registerConfig(File file) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "Registering configuration " + file.getName());
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();

        if (!file.exists()) {
            try{
                file.createNewFile();
                plugin.getLogger().info(ChatColor.GREEN + file.getName() + " has been created");
            } catch (IOException e) {
                plugin.getLogger().severe(file.getName() + " could not be created");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
