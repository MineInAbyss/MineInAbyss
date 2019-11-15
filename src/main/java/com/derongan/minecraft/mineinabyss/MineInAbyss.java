package com.derongan.minecraft.mineinabyss;

import com.derongan.minecraft.guiy.GuiListener;
import com.derongan.minecraft.mineinabyss.GUI.GUICommandExecutor;
import com.derongan.minecraft.mineinabyss.ascension.AscensionCommandExecutor;
import com.derongan.minecraft.mineinabyss.ascension.AscensionListener;
import com.derongan.minecraft.mineinabyss.configuration.ConfigurationManager;
import com.derongan.minecraft.mineinabyss.player.PlayerData;
import com.derongan.minecraft.mineinabyss.player.PlayerDataConfigManager;
import com.derongan.minecraft.mineinabyss.player.PlayerListener;
import com.derongan.minecraft.mineinabyss.world.WorldCommandExecutor;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class MineInAbyss extends JavaPlugin {
    private static AbyssContext context;
    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;
    private final int TICKS_BETWEEN = 5;
    private ConfigurationManager configManager;

    public static Economy getEcon() {
        return econ;
    }

    public static Permission getPerms() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }

    public static MineInAbyss getInstance() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MineInAbyss");

        return (MineInAbyss) plugin;
    }

    public static AbyssContext getContext() {
        return context;
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("On enable has been called");

        //Vault setup
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
//        setupPermissions();
//        setupChat();

        loadConfigManager();

        context = new AbyssContext(this);

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(context), this);
        getServer().getPluginManager().registerEvents(new AscensionListener(context), this);

        PlayerDataConfigManager manager = new PlayerDataConfigManager(context);

        getServer().getOnlinePlayers().forEach((player) ->
                context.getPlayerDataMap().put(
                        player.getUniqueId(),
                        manager.loadPlayerData(player))
        );

        WorldCommandExecutor worldCommandExecutor = new WorldCommandExecutor(context);
        AscensionCommandExecutor ascensionCommandExecutor = new AscensionCommandExecutor(context);
        GUICommandExecutor guiCommandExecutor = new GUICommandExecutor(context);

        this.getCommand("curseon").setExecutor(ascensionCommandExecutor);
        this.getCommand("curseoff").setExecutor(ascensionCommandExecutor);
        this.getCommand("stats").setExecutor(guiCommandExecutor);
        this.getCommand("start").setExecutor(guiCommandExecutor);
        this.getCommand("creategondolaspawn").setExecutor(guiCommandExecutor);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PlayerDataConfigManager manager = new PlayerDataConfigManager(context);

        getServer().getOnlinePlayers().forEach(player -> {
            PlayerData data = context.getPlayerDataMap().get(player.getUniqueId());
            try {
                manager.savePlayerData(data);
            } catch (IOException e) {
                getLogger().warning("Error saving player data for " + player.getUniqueId());
                e.printStackTrace();
            }
        });
        configManager.saveSpawnLocConfig();
        getLogger().info("onDisable has been invoked!");
    }

    //economy stuff
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public void loadConfigManager() {
        configManager = new ConfigurationManager(this);
    }
}
