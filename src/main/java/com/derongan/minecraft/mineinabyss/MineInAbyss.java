package com.derongan.minecraft.mineinabyss;

import com.derongan.minecraft.guiy.GuiListener;
import com.derongan.minecraft.mineinabyss.ascension.AscensionCommandExecutor;
import com.derongan.minecraft.mineinabyss.ascension.AscensionListener;
import com.derongan.minecraft.mineinabyss.commands.GUICommandExecutor;
import com.derongan.minecraft.mineinabyss.player.PlayerListener;
import com.derongan.minecraft.mineinabyss.commands.WorldCommandExecutor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineInAbyss extends JavaPlugin {
    private static AbyssContext context;
    private static Economy econ = null;
    private final int TICKS_BETWEEN = 5;

    public static Economy getEcon() {
        return econ;
    }

    public static MineInAbyss getInstance() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MineInAbyss");

        return (MineInAbyss) plugin;
    }

    public static AbyssContext getContext() {
        return context;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("On enable has been called");

        context = new AbyssContext(this);

        //Vault setup
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(context), this);
        getServer().getPluginManager().registerEvents(new AscensionListener(context), this);

        WorldCommandExecutor worldCommandExecutor = new WorldCommandExecutor(context);
        AscensionCommandExecutor ascensionCommandExecutor = new AscensionCommandExecutor(context);
        GUICommandExecutor guiCommandExecutor = new GUICommandExecutor(context);

        this.getCommand("curseon").setExecutor(ascensionCommandExecutor);
        this.getCommand("curseoff").setExecutor(ascensionCommandExecutor);
        this.getCommand("stats").setExecutor(guiCommandExecutor);
        this.getCommand("start").setExecutor(guiCommandExecutor);
        this.getCommand("leave").setExecutor(guiCommandExecutor);
        this.getCommand("creategondolaspawn").setExecutor(guiCommandExecutor);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        context.getConfigManager().saveAll();
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
}
