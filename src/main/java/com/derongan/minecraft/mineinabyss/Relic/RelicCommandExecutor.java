package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.Relic.Relics.BlazeReapRelicType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RelicCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (label.equals("give")) {
                player.getInventory().addItem(new BlazeReapRelicType().getItem());
            }

            return true;
        }

        return true;
    }
}
