package com.derongan.minecraft.mineinabyss.GUI;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUICommandExecutor implements CommandExecutor {
    private AbyssContext context;

    public GUICommandExecutor(AbyssContext context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (label.equals("stats")) {
                new StatsGUI(player, JavaPlugin.getPlugin(MineInAbyss.class)).show(player);
                return true;
            } else if (label.equals("start") && player.hasPermission("mineinabyss.start")) {
                new GondolaGUI(player, JavaPlugin.getPlugin(MineInAbyss.class)).show(player);
                return true;
            } else if (label.equals("creategondolaspawn") && player.hasPermission("mineinabyss.createspawn")) {
                FileConfiguration spawnLocConfig = context.getConfigManager().getSpawnLocConfig();
                List<Map<?, ?>> spawns = spawnLocConfig.getMapList(GondolaGUI.SPAWN_KEY);
                ItemStack displayItem = player.getInventory().getItemInMainHand().clone();

                if (displayItem.getType().equals(Material.AIR))
                    displayItem = new ItemStack(Material.GRASS_BLOCK);

                ItemMeta meta = displayItem.getItemMeta();
                if (args.length > 0) meta.setDisplayName(args[0]);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                displayItem.setItemMeta(meta);

                Map<String, Object> map = new HashMap<>();
                map.put("location", player.getLocation());
                map.put("display-item", displayItem);
                if (args.length >= 2)
                    map.put("cost", args[1]);

                spawns.add(map);
                spawnLocConfig.set(GondolaGUI.SPAWN_KEY, spawns);
                context.getConfigManager().saveSpawnLocConfig();
                player.sendMessage("Created spawn");
                return true;
            }
        }

        return false;
    }
}
