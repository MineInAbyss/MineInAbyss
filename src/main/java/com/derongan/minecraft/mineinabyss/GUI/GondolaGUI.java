package com.derongan.minecraft.mineinabyss.GUI;

import com.derongan.minecraft.deeperworld.world.WorldManager;
import com.derongan.minecraft.guiy.gui.*;
import com.derongan.minecraft.guiy.gui.layouts.HistoryGuiHolder;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManager;
import com.derongan.minecraft.mineinabyss.world.Layer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GondolaGUI extends HistoryGuiHolder {
    public static final String SPAWN_KEY = "spawns";
    private static ChatColor mainColor = ChatColor.RED;
    private static ChatColor secondaryColor = ChatColor.GOLD;
//    private Map<World, ArrayList<Location>> spawnLocations = new HashMap<>();
    private AbyssContext context;

    public GondolaGUI(Player player, MineInAbyss plugin) {
        super(6, "Choose Spawn Location", plugin);
        context = MineInAbyss.getContext();
        this.player = player;

        setElement(buildMain());
    }

    private Layout buildMain() {
        Layout layout = new Layout();
        FileConfiguration spawnLocConfig = context.getConfigManager().getSpawnLocConfig();

        //TODO separate spawns into groups based on world
        FillableElement grid = new FillableElement(5, 9);

        List<Map<?, ?>> spawnLayers = spawnLocConfig.getMapList(SPAWN_KEY);
        spawnLayers.forEach(spawn -> grid.addElement(parseLayer(spawn)));

        layout.addElement(0, 1, grid);
        addBackButton(layout);
        return layout;
    }

    private Element parseLayer(Map<?, ?> map) {
        double cost = 0;
        if (map.containsKey("cost"))
            cost = Double.parseDouble((String) map.get("cost"));
        ItemStack displayItem = ((ItemStack) map.get("display-item")).clone(); //TODO convert to key variable
        Bukkit.broadcastMessage(displayItem.toString());
        ItemMeta itemMeta = displayItem.getItemMeta();

        Location loc = (Location) map.get("location");
        if (MineInAbyss.getEcon().getBalance(player) >= cost) {
            itemMeta.setLore(Arrays.asList(ChatColor.GOLD + "$" + cost));
            displayItem.setItemMeta(itemMeta);

            ClickableElement button = new ClickableElement(Cell.forItemStack(displayItem));
            //TODO this should show a fancy title when you click it and check whether you have enough money to afford it
            double finalCost = cost;
            button.setClickAction(clickEvent -> {
                player.teleport(loc);
                AbyssWorldManager worldManager = MineInAbyss.getContext().getWorldManager();
                WorldManager realWorldManager = MineInAbyss.getContext().getRealWorldManager();
                Layer layer = worldManager.getLayerForSection(realWorldManager.getSectionFor(loc));
                player.sendTitle(layer.getName(), layer.getSub(), 50, 10, 20);
                MineInAbyss.getEcon().withdrawPlayer(player, finalCost);

                Bukkit.getScheduler().scheduleSyncDelayedTask(MineInAbyss.getInstance(), () -> {
                    player.sendTitle("", String.format("%s%sLet the journey begin", ChatColor.GRAY, ChatColor.ITALIC), 30, 30, 20);
                }, 80);

            });
            return button;
        } else {
            displayItem.setType(Material.BEDROCK);
            itemMeta.setDisplayName(ChatColor.STRIKETHROUGH + itemMeta.getDisplayName());
            itemMeta.setLore(Arrays.asList(ChatColor.RED + "Cannot Afford: $" + cost));
            displayItem.setItemMeta(itemMeta);
            return Cell.forItemStack(displayItem);
        }
    }
}
