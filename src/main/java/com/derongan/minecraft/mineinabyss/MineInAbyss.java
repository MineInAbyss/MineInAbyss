package com.derongan.minecraft.mineinabyss;

import com.derongan.minecraft.guiy.GuiListener;
import com.derongan.minecraft.mineinabyss.ascension.AscensionListener;
import com.derongan.minecraft.mineinabyss.commands.AscensionCommandExecutor;
import com.derongan.minecraft.mineinabyss.commands.GUICommandExecutor;
import com.derongan.minecraft.mineinabyss.geary.AbyssLocationSystem;
import com.derongan.minecraft.mineinabyss.geary.DepthMeter;
import com.derongan.minecraft.mineinabyss.player.PlayerListener;
import com.google.common.collect.ImmutableSet;
import com.mineinabyss.geary.GearyService;
import com.mineinabyss.geary.ecs.component.components.equipment.Durability;
import com.mineinabyss.geary.ecs.component.components.grappling.GrapplingHook;
import com.mineinabyss.geary.ecs.component.components.rendering.DisplayState;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

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
            getLogger().severe(String
                    .format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //Geary setup
        if (setupGeary()) {
            NamespacedKey grapplingRecipeKey = new NamespacedKey(this, "grappling_hook");
            NamespacedKey starCompassRecipeKey = new NamespacedKey(this, "star_compas");

            Iterator<Recipe> recipeIterator = getServer().recipeIterator();

            // Remove recipes if already loaded. This way changes will take effect properly.
            // TODO change to config based (in geary or mine in abyss) instead of manual.
            while (recipeIterator.hasNext()) {
                Recipe r = recipeIterator.next();
                if (r instanceof ShapedRecipe) {
                    if (ImmutableSet.of(grapplingRecipeKey, starCompassRecipeKey)
                            .contains(((ShapedRecipe) r).getKey())) {
                        recipeIterator.remove();
                    }
                }
            }

            getServer().addRecipe(getGrapplingHookRecipe(grapplingRecipeKey));
            getServer().addRecipe(getStarCompassRecipe(starCompassRecipeKey));

            gearyService.addSystem(new AbyssLocationSystem(context), this);
        } else {
            getLogger().warning(String
                    .format("[%s] - Geary service not found! No items have been added!",
                            getDescription().getName()));

        }

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(context), this);
        getServer().getPluginManager().registerEvents(new AscensionListener(), this);

        //register command executors
        new AscensionCommandExecutor();
        new GUICommandExecutor();
    }

    @NotNull
    private ShapedRecipe getGrapplingHookRecipe(NamespacedKey grapplingRecipeKey) {
        ItemStack itemStack = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("Grappling Hook");
        itemMeta.setCustomModelData(3);
        itemStack.setItemMeta(itemMeta);

        gearyService.attachToItemStack(ImmutableSet
                .of(new GrapplingHook(1.3, 3, 4, Color.fromRGB(142, 89, 60), 1),
                        new Durability(64), new DisplayState(3)), itemStack);

        ShapedRecipe recipe = new ShapedRecipe(grapplingRecipeKey, itemStack);

        recipe.shape("III", "ISI", " S ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STRING);
        return recipe;
    }

    @NotNull
    private ShapedRecipe getStarCompassRecipe(NamespacedKey starCompassRecipeKey) {
        ItemStack itemStack = new ItemStack(Material.COMPASS);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("Star Compass");
        itemStack.setItemMeta(itemMeta);

        gearyService.attachToItemStack(ImmutableSet.of(new DepthMeter(250), new DisplayState(1)), itemStack);

        ShapedRecipe recipe = new ShapedRecipe(starCompassRecipeKey, itemStack);

        recipe.shape(" I ", "IPI", " I ");
        recipe.setIngredient('I', Material.GLASS);
        recipe.setIngredient('P', Material.PRISMARINE_CRYSTALS);
        return recipe;
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
        if (getServer().getPluginManager().isPluginEnabled("Geary")) {
            RegisteredServiceProvider<GearyService> rsp = getServer().getServicesManager().getRegistration(GearyService.class);
            if (rsp == null) {
                return false;
            }

            gearyService = rsp.getProvider();
            return gearyService != null;
        }

        return false;
    }
}
