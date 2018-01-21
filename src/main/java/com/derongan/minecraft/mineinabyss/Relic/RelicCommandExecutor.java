package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Relics.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RelicCommandExecutor implements CommandExecutor {
    private AbyssContext context;

    public RelicCommandExecutor(AbyssContext context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (label.equals("relic")) {
                switch (args[0]) {
                    case "blazereap":
                        player.getInventory().addItem(new BlazeReapRelicType(context).getItem());
                        break;
                    case "pusher":
                        player.getInventory().addItem(new PushStickRelicType(context).getItem());
                        break;
                    case "bell":
                        player.getInventory().addItem(new UnheardBellRelicType(context).getItem());
                        break;
                    case "compass":
                        player.getInventory().addItem(new StarCompassArtifactType(context).getItem());
                        break;
                    case "incinerator":
                        player.getInventory().addItem(new IncineratorRelicType(context).getItem());
                        break;
                    case "hook":
                        player.getInventory().addItem(new GrapplingHookRelicType(context).getItem());
                        break;
                }
            }

            return true;
        }

        return true;
    }
}
