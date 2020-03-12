package com.derongan.minecraft.mineinabyss.commands;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldCommandExecutor implements CommandExecutor {
    private AbyssContext context;

    public WorldCommandExecutor(AbyssContext context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (label.equals("sectionon")) {
                context.getPlayerData(player).setAnchored(false);
                player.sendMessage("Automatic TP enabled");
                return true;
            }

            if (label.equals("sectionoff")) {
                context.getPlayerData(player).setAnchored(true);
                player.sendMessage("Automatic TP disabled");
                return true;
            }
        }
        return false;
    }
}
