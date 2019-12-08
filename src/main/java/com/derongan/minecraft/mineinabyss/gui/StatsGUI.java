package com.derongan.minecraft.mineinabyss.gui;

import com.derongan.minecraft.deeperworld.world.section.Section;
import com.derongan.minecraft.guiy.gui.*;
import com.derongan.minecraft.guiy.gui.layouts.HistoryGuiHolder;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import com.derongan.minecraft.mineinabyss.configuration.ConfigConstants;
import com.derongan.minecraft.mineinabyss.player.PlayerData;
import de.erethon.headlib.HeadLib;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsGUI extends HistoryGuiHolder {
    private AbyssContext context;
    private List<ClickableElement> mobConfigs = new ArrayList<>();
    private List<ClickableElement> spawnList = new ArrayList<>();
    private PlayerData playerData;
    private static ChatColor mainColor = ConfigConstants.mainColor;
    private static ChatColor secondaryColor = ConfigConstants.secondaryColor;

    public StatsGUI(Player player, MineInAbyss plugin) {
        super(6, "Mine In Abyss - Stats", plugin);
        context = MineInAbyss.getContext();
        playerData = context.getPlayerData(player);
        this.player = player;

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

    private Layout buildMain() {
        Layout layout = new Layout();

        //Player head
        Element name = Cell.forItemStack(getHead(player), mainColor + player.getName());
        layout.addElement(0, 0, name);

        //Player's whistle level
        ItemStack whistleItem = playerData.getWhistle().getItem();
        Element whistle = Cell.forItemStack(whistleItem, mainColor + "Whisle: " + whistleItem.getItemMeta().getDisplayName());
        layout.addElement(1, 0, whistle);

        //The section the player is currently in
        Section section = context.getRealWorldManager().getSectionFor(player.getLocation());
        String layerName = "Not in a layer", sectionName = "Not in a section";
        if (section != null) {
            layerName = context.getWorldManager().getLayerForSection(section).getName();
            sectionName = section.getKey().toString().toUpperCase();
        }

        ItemStack layerHead = HeadLib.QUARTZ_L.toItemStack(mainColor + "Layer: " + secondaryColor + layerName);
        ItemMeta layerHeadMeta = layerHead.getItemMeta();
        layerHeadMeta.setLore(Arrays.asList(mainColor + "Section: " + secondaryColor + sectionName));
        layerHead.setItemMeta(layerHeadMeta);

        Element layer = Cell.forItemStack(layerHead);
        layout.addElement(2, 0, layer);

        //The player's level
        Element level = Cell.forItemStack(new ItemStack(Material.EXPERIENCE_BOTTLE), mainColor + "Level: " + ChatColor.GREEN + context.getPlayerData(player).getLevel());
        layout.addElement(7, 0, level);

        //The player's balance
        Element balance = Cell.forItemStack(new ItemStack(Material.GOLD_BLOCK), mainColor + "Balance: $" + ChatColor.GOLD + MineInAbyss.getEcon().getBalance(player));
        layout.addElement(8, 0, balance);

        addBackButton(layout);
        return layout;
    }
}
