package com.derongan.minecraft.mineinabyss.ascension;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class AscensionCommandExecutor implements CommandExecutor {
    private AbyssContext context;

    public AscensionCommandExecutor(AbyssContext context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if(!player.hasPermission(Permissions.TOGGLE_CURSE)){
                player.sendMessage(ChatColor.RED + "The abyss laughs at your naivety");
                return true;
            }

            if (label.equals("curseon") && player.hasPermission(Permissions.TOGGLE_CURSE)) {
                context.getPlayerDataMap().get(player.getUniqueId()).setAffectedByCurse(true);
                player.sendMessage("Curse enabled");
                return true;
            }

            if (label.equals("curseoff") && player.hasPermission(Permissions.TOGGLE_CURSE)){
                context.getPlayerDataMap().get(player.getUniqueId()).setAffectedByCurse(false);
                player.sendMessage("Curse disabled");
                return true;
            }
        }

        return false;
    }
}
