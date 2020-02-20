package com.derongan.minecraft.mineinabyss.configuration;

import com.derongan.minecraft.mineinabyss.MineInAbyss;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager extends YamlConfiguration {
    private MineInAbyss plugin;
    private File file;

    public ConfigManager(MineInAbyss plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        reload();
    }

    public void reload() {
        registerConfig();
    }

    public void saveConfig() {
        try {
            save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerConfig() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "Registering configuration " + file.getName());
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();

        if (!file.exists()) {
            try {
                file.createNewFile();
                plugin.getLogger().info(ChatColor.GREEN + file.getName() + " has been created");
            } catch (IOException e) {
                plugin.getLogger().severe(file.getName() + " could not be created");
            }
        }

        try {
            load(file);
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }
    }
}
