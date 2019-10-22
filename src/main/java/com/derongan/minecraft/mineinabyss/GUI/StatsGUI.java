package com.derongan.minecraft.mineinabyss.GUI;

import com.derongan.minecraft.deeperworld.world.section.Section;
import com.derongan.minecraft.guiy.gui.*;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.derongan.minecraft.mineinabyss.player.PlayerData;
import de.erethon.headlib.HeadLib;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsGUI extends GuiHolder {
    private Player player;
    //    private Layout spawns;
    private MineInAbyss plugin;
    private AbyssContext context;
    private List<Layout> history = new ArrayList<>();
    private ClickableElement back;
    private List<ClickableElement> mobConfigs = new ArrayList<>();
    private List<ClickableElement> spawnList = new ArrayList<>();
    private FileConfiguration config;
    private PlayerData playerData;

    public StatsGUI(Player player, MineInAbyss plugin) {
        super(6, "Mine In Abyss - Stats", plugin);
        this.plugin = plugin;
        context = MineInAbyss.getContext();
        playerData = context.getPlayerData(player);
        this.player = player;

        //create back button
        Element cell = Cell.forItemStack(HeadLib.RED_X.toItemStack("Back"));
        back = new ClickableElement(cell);
        back.setClickAction(clickEvent -> backInHistory());

        setElement(buildMain());
    }

    public static ItemStack getHead(Player player) {
        int lifePlayer = (int) player.getHealth();
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(ChatColor.RESET + player.getName());
        skull.setOwningPlayer(player);
        item.setItemMeta(skull);
        return item;
    }

    public Player getPlayer() {
        return player;
    }

    public void addBackButton(Layout layout) {
        history.add(layout);
        layout.addElement(8, 5, back);
    }

    public void backInHistory() {
        if (history.size() <= 1) {
            player.closeInventory();
            return;
        }
        Layout previous = history.get(history.size() - 2);
        history.remove(history.size() - 1);

        setElement(previous);
    }

    private Layout buildMain() {
        Layout layout = new Layout();

        //Player head
        Element name = Cell.forItemStack(getHead(player), player.getName());
        layout.addElement(0, 0, name);

        //Player's whistle level
        Element whistle = Cell.forItemStack(playerData.getWhistle().getItem());
        layout.addElement(1, 0, whistle);

        //The section the player is currently in
        Section section = context.getRealWorldManager().getSectionFor(player.getLocation());
        String layerName = "Not in a layer", sectionName = "Not in a section";
        if (section != null) {
            layerName = context.getWorldManager().getLayerForSection(section).getName();
            sectionName = "Section: " + section.getKey().toString().toUpperCase();
        }

        ItemStack layerHead = HeadLib.QUARTZ_L.toItemStack("Layer");
        ItemMeta layerHeadMeta = layerHead.getItemMeta();
        layerHeadMeta.setLore(Arrays.asList(layerName, sectionName));
        layerHead.setItemMeta(layerHeadMeta);

        Element layer = Cell.forItemStack(layerHead);
        layout.addElement(2, 0, layer);

        addBackButton(layout);
        return layout;
    }
}
