package com.derongan.minecraft.mineinabyss.ascension;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Permissions;
import com.derongan.minecraft.mineinabyss.commands.CommandLabels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AscensionCommandExecutor implements CommandExecutor {
    private AbyssContext context;

    public AscensionCommandExecutor(AbyssContext context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (!player.hasPermission(Permissions.TOGGLE_CURSE)) {
                player.sendMessage(ChatColor.RED + "The abyss laughs at your naivety");
                return true;
            }

            if (label.equals(CommandLabels.CURSEON) && (player.hasPermission(Permissions.TOGGLE_CURSE) || player.hasPermission(Permissions.CURSE_OFF))) {
                context.getPlayerData(player).setAffectedByCurse(true);
                player.sendMessage("Curse enabled");
                return true;
            }

            if (label.equals(CommandLabels.CURSEOFF) && (player.hasPermission(Permissions.TOGGLE_CURSE) || player.hasPermission(Permissions.CURSE_ON))) {
                context.getPlayerData(player).setAffectedByCurse(false);
                player.sendMessage("Curse disabled");
                return true;
            }
        }

        return false;
    }
}
