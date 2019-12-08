package com.derongan.minecraft.mineinabyss;

import com.derongan.minecraft.deeperworld.world.WorldManager;
import com.derongan.minecraft.mineinabyss.configuration.ConfigurationManager;
import com.derongan.minecraft.mineinabyss.player.PlayerData;
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManager;
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManagerImpl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Stores context for the plugin, such as the plugin instance
 */
public class AbyssContext {
    private Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private MineInAbyss plugin;
    private Logger logger;
    private Configuration config;
    private ConfigurationManager configManager;
    private WorldManager realWorldManager;
    private AbyssWorldManager worldManager;

    public AbyssContext(MineInAbyss plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
        logger = plugin.getLogger();
        configManager = plugin.getConfigManager();
        worldManager = new AbyssWorldManagerImpl(getConfig());
        realWorldManager = Bukkit.getServicesManager().load(WorldManager.class);
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public MineInAbyss getPlugin() {
        return plugin;
    }

    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerDataMap().get(player.getUniqueId());
    }

    public Logger getLogger() {
        return logger;
    }

    public Configuration getConfig() {
        return config;
    }

    public AbyssWorldManager getWorldManager() {
        return worldManager;
    }

    public WorldManager getRealWorldManager() {
        return realWorldManager;
    }
}
