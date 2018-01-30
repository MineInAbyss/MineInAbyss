package com.derongan.minecraft.mineinabyss.Ascension;

import com.derongan.minecraft.mineinabyss.AbyssContext;
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
            AscensionData data = context.getPlayerAcensionDataMap().get(player.getUniqueId());

            if (label.equals("sectionon")) {
                data.setDev(false);
                return true;
            } else if (label.equals("sectionoff")) {
                data.setDev(true);
                return true;
            }
        }

        return false;
    }
}
