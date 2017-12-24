package com.derongan.minecraft.madeinabyss.Ascension;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AscensionTask extends BukkitRunnable{
    private AbyssContext context;
    private int ticksBetween;

    public AscensionTask(AbyssContext context, int ticksBetween) {
        this.context = context;
        this.ticksBetween = ticksBetween;
    }

    @Override
    public void run() {
        context.getPlayerAcensionDataMap().forEach((uuid, data) ->{
            Player player = context.getPlugin().getServer().getPlayer(uuid);
            if(player != null && player.isOnline()){
                try {
                    data.getCurrentEffects().forEach(ascensionEffect -> ascensionEffect.applyEffect(player, ticksBetween));
                    data.removeFinishedEffects(player);
                } catch (NullPointerException npe){
                    context.getLogger().info("Player disconnected");
                }
            }
        });
    }
}
