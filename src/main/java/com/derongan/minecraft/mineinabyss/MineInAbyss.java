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
import com.google.common.collect.Iterators;
import com.mineinabyss.geary.Geary;
import com.mineinabyss.geary.PredefinedArtifacts;
import com.mineinabyss.geary.core.ItemUtil.EntityInitializer;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineInAbyss extends JavaPlugin implements Listener {

  private static AbyssContext context;
  private final int TICKS_BETWEEN = 5;
  private ShapedRecipe recipe;

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
    ConfigurationManager.createConfig(this);

    context = new AbyssContext(getConfig());
    context.setPlugin(this);
    context.setLogger(getLogger());

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

    if (getServer().getPluginManager().isPluginEnabled("Geary")) {
      Geary geary = (Geary) getServer().getPluginManager().getPlugin("Geary");
      EntityInitializer entityInitializer = () -> PredefinedArtifacts
          .createGrapplingHook(1.2, 3, 4,
              Color.YELLOW, 1, 25);

      ItemStack itemStack = new ItemStack(Material.DIAMOND_SHOVEL);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(String.format("Grappling Hook"));

      itemMeta.setCustomModelData(3);
      itemStack.setItemMeta(itemMeta);

      recipe = geary
          .createRecipe(new NamespacedKey(this, "grappling_hook"), entityInitializer, itemStack);

      this.recipe.shape("III", "ISI", " S ");
      this.recipe.setIngredient('S', Material.STRING);
      this.recipe.setIngredient('I', Material.IRON_INGOT);
      getServer().addRecipe(this.recipe);

      getServer().getPluginManager().registerEvents(this, this);
    }
  }

  @EventHandler
  void onPluginDisable(PluginDisableEvent disable) {
    if (disable.getPlugin().getClass() == Geary.class) {
      Iterators.removeIf(getServer().recipeIterator(), input -> input.equals(recipe));
    }
  }

  @EventHandler
  void onPluginEnable(PluginEnableEvent enable) {
    if (enable.getPlugin().getClass() == Geary.class) {
      if (!Iterators.any(getServer().recipeIterator(), input -> input.equals(recipe))) {
        getServer().addRecipe(recipe);
      }
    }
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
    getLogger().info("onDisable has been invoked!");
  }
}
