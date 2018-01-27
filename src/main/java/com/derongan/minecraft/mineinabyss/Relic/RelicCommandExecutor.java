package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
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
//                try {
//                    RelicType type = StandardRelicType.valueOf(args[0]);
//                    player.getInventory().addItem(type.getItem());
//                } catch (IllegalArgumentException e){
//                    return false;
//                }

                if (args.length == 0) {
                    return false;
                }
                for (RelicType relicType : RelicType.registeredRelics.values()) {
                    if (relicType.getName().replace(" ", "_").toLowerCase().equals(args[0].toLowerCase())) {
                        player.getInventory().addItem(relicType.getItem());
                        return true;
                    }
                }
            }
            //if (label.equals("asPos")) {
            //    commandSender.
            //}

            if (label.equals("relicreload")) {
                RelicLoader.unloadAllRelics();
                RelicLoader.loadAllRelics(context);
                return true;
            }
        }

        return false;
    }
}
