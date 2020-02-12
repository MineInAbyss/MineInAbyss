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
import com.mineinabyss.geary.Geary;
import com.mineinabyss.geary.PredefinedArtifacts;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineInAbyss extends JavaPlugin implements Listener {

  private static AbyssContext context;
  private final int TICKS_BETWEEN = 5;
  private ShapedRecipe recipe;
  private Geary geary;

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
      geary = (Geary) getServer().getPluginManager().getPlugin("Geary");

      NamespacedKey key = new NamespacedKey(this, "grappling_hook");

      ItemStack itemStack = new ItemStack(Material.DIAMOND_SHOVEL);
      ItemMeta itemMeta = itemStack.getItemMeta();
      itemMeta.setDisplayName(String.format("Grappling Hook"));

      itemMeta.setCustomModelData(3);
      itemStack.setItemMeta(itemMeta);

      recipe = new ShapedRecipe(key, itemStack);
      recipe.shape("III", "ISI", " S ");
      recipe.setIngredient('S', Material.STRING);
      recipe.setIngredient('I', Material.IRON_INGOT);
      getServer().addRecipe(recipe);

      getServer().getPluginManager().registerEvents(this, this);
    }
  }

  @EventHandler
  void onCraftItem(CraftItemEvent craftItemEvent) {
    if (craftItemEvent.getRecipe().getResult().hasItemMeta() && craftItemEvent.getRecipe()
        .getResult().getItemMeta().getDisplayName().equals("Grappling Hook")) {
      geary.attach(
          () -> PredefinedArtifacts
              .createGrapplingHook(1.5, 3, 4, Color.fromRGB(142, 89, 60), 1, 25),
          craftItemEvent.getInventory().getResult());
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
