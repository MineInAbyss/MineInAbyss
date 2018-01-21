package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.Relic.Relics.BlazeReapRelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.PushStickRelicType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RelicCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (label.equals("relic")) {
                switch (args[0]) {
                    case "blazereap":
                        player.getInventory().addItem(new BlazeReapRelicType().getItem());
                        break;
                    case "pusher":
                        player.getInventory().addItem(new PushStickRelicType().getItem());
                        break;
                }
            }

            return true;
        }

        return true;
    }
}
