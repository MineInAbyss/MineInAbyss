package com.derongan.minecraft.mineinabyss.Ascension;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AscensionCommandListener implements CommandExecutor {
    private AbyssContext context;

    public AscensionCommandListener(AbyssContext context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (label.equals("curseon")) {
                context.getPlayerDataMap().get(player.getUniqueId()).setAffectedByCurse(true);
                player.sendMessage("Curse enabled");
                return true;
            }

            if (label.equals("curseoff")){
                context.getPlayerDataMap().get(player.getUniqueId()).setAffectedByCurse(false);
                player.sendMessage("Curse disabled");
                return true;
            }
        }

        return false;
    }
}
