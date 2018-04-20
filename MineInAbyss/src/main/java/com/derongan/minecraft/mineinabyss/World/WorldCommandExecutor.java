package com.derongan.minecraft.mineinabyss.World;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Loading.RelicLoader;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

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
                context.getPlayerDataMap().get(player.getUniqueId()).setAnchored(false);
                player.sendMessage("Automatic TP enabled");
                return true;
            }

            if (label.equals("sectionoff")){
                context.getPlayerDataMap().get(player.getUniqueId()).setAnchored(true);
                player.sendMessage("Automatic TP disabled");
                return true;
            }
        }

        return false;
    }
}
