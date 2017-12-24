package com.derongan.minecraft.madeinabyss.Ascension;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import com.derongan.minecraft.madeinabyss.Ascension.Effect.DeathAscensionEffect;
import com.derongan.minecraft.madeinabyss.Layer.Layer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AscensionListener implements Listener {
    private AbyssContext context;

    private double distanceForEffect = 5;

    public AscensionListener(AbyssContext context) {
        this.context = context;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        double deltaY = playerMoveEvent.getTo().getY() - playerMoveEvent.getFrom().getY();
        Player player = playerMoveEvent.getPlayer();

        Layer currentLayer = context.getLayerMap().getOrDefault(player.getWorld().getName(), null);

        if (currentLayer != null) {
            AscensionData data = context.getPlayerAcensionDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new AscensionData());
            data.changeDistanceMovedUp(deltaY);

            if (data.getDistanceMovedUp() >= currentLayer.getOffset()) {
                data.setDistanceMovedUp(data.getDistanceMovedUp() % distanceForEffect);
                currentLayer.getEffectsOnLayer().forEach(effectBuilder -> {
                    data.applyEffect(effectBuilder.build());
                });
            }
            ;
        }
    }


    //TODO can I get a better cause reason
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        AscensionData deadPlayerData = context.getPlayerAcensionDataMap().get(playerDeathEvent.getEntity().getUniqueId());
        String layerOfDeathName = playerDeathEvent.getEntity().getWorld().getName();
        String playerName = playerDeathEvent.getEntity().getName();
        Layer layerOfDeath = context.getLayerMap().getOrDefault(layerOfDeathName, null);

        if (layerOfDeath.getDeathMessage() != null || deadPlayerData.getCurrentEffects().stream().anyMatch(a -> a instanceof DeathAscensionEffect) || (playerDeathEvent.getDeathMessage().contains("withered away"))) {
            playerDeathEvent.setDeathMessage(layerOfDeath.getDeathMessage().replace("{Player}", playerName));
        } else {
            // TODO need to make this flow with current layer.
            playerDeathEvent.setDeathMessage(playerDeathEvent.getDeathMessage() + " in the depths of the abyss");
        }

        deadPlayerData.clearEffects(playerDeathEvent.getEntity());
        deadPlayerData.setDistanceMovedUp(0);
    }
}
