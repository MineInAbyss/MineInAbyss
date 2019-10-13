package com.derongan.minecraft.mineinabyss.GUI;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
            }
        }

        return false;
    }
}
