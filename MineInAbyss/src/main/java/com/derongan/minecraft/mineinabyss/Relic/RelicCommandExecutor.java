package com.derongan.minecraft.mineinabyss.Relic;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Relic.Loading.RelicLoader;
import com.derongan.minecraft.mineinabyss.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.Relic.Relics.StandardRelicType;
import com.derongan.minecraft.mineinabyss.World.ChunkEntity;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

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

            if (label.equals("relicreload")) {
                RelicLoader.unloadAllRelics();
                RelicLoader.loadAllRelics(context);
                return true;
            }

            if(label.equals("relics")){
                player.sendMessage("Relics: " + RelicType.registeredRelics.values().stream()
                        .map(RelicType::getName)
                        .map(a->a.replace(  " ", "_"))
                        .collect(Collectors.joining(", ")));
                return true;
            }

            if(label.equals("yolo")){
                Location location = player.getLocation();
                RelicType blaze = StandardRelicType.BLAZE_REAP;
                ChunkEntity entity = new RelicGroundEntity(blaze,
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ());
                entity.createEntity(location.getWorld());

                context.getEntityChunkManager().addEntity(location.getChunk(), entity);
            }

            if (label.equals("pinsreset")) {
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1);
                player.sendMessage("Thousand men pins removed");
                return true;
            }
        }

        return false;
    }
}
