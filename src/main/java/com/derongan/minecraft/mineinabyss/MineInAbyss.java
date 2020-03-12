package com.derongan.minecraft.mineinabyss;

import com.derongan.minecraft.guiy.GuiListener;
import com.derongan.minecraft.mineinabyss.ascension.AscensionCommandExecutor;
import com.derongan.minecraft.mineinabyss.ascension.AscensionListener;
import com.derongan.minecraft.mineinabyss.commands.CommandLabels;
import com.derongan.minecraft.mineinabyss.commands.GUICommandExecutor;
import com.derongan.minecraft.mineinabyss.commands.WorldCommandExecutor;
import com.derongan.minecraft.mineinabyss.player.PlayerListener;
import com.mineinabyss.geary.GearyService;
import com.mineinabyss.geary.PredefinedArtifacts;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineInAbyss extends JavaPlugin {
    private static AbyssContext context;
    private static Economy econ = null;
    private GearyService gearyService;

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

        //Geary setup
        if (setupGeary()) {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SHOVEL);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setCustomModelData(3);
            itemMeta.setDisplayName("Grappling Hook");
            itemStack.setItemMeta(itemMeta);
            ShapedRecipe recipe = gearyService.createRecipe(new NamespacedKey(this, "grappling_hook"),
                () -> PredefinedArtifacts
                    .createGrapplingHook(1.3, 3, 4, Color.fromRGB(142, 89, 60), 1, 64), itemStack);

            recipe.shape("III", "ISI", " S ");
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('S', Material.STRING);

            getServer().addRecipe(recipe);
        } else {
            getLogger().warning(String
                .format("[%s] - Geary service not found! No items have been added!",
                    getDescription().getName()));

        }

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(context), this);
        getServer().getPluginManager().registerEvents(new AscensionListener(), this);

        WorldCommandExecutor worldCommandExecutor = new WorldCommandExecutor(context);
        AscensionCommandExecutor ascensionCommandExecutor = new AscensionCommandExecutor();
        GUICommandExecutor guiCommandExecutor = new GUICommandExecutor();

        this.getCommand(CommandLabels.CURSEON).setExecutor(ascensionCommandExecutor);
        this.getCommand(CommandLabels.CURSEOFF).setExecutor(ascensionCommandExecutor);
        this.getCommand(CommandLabels.STATS).setExecutor(guiCommandExecutor);
        this.getCommand(CommandLabels.START).setExecutor(guiCommandExecutor);
        this.getCommand(CommandLabels.STOP_DESCENT).setExecutor(guiCommandExecutor);
        this.getCommand(CommandLabels.CREATE_GONDOLA_SPAWN).setExecutor(guiCommandExecutor);
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

    private boolean setupGeary() {
        if(getServer().getPluginManager().isPluginEnabled("Geary")) {
            RegisteredServiceProvider<GearyService> rsp = getServer().getServicesManager().getRegistration(GearyService.class);
            if(rsp == null){
                return false;
            }

            gearyService = rsp.getProvider();
            return gearyService != null;
        }

        return false;
    }
}
