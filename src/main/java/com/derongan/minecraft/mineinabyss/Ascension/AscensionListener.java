package com.derongan.minecraft.mineinabyss.Ascension;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects.DeathAscensionEffect;
import com.derongan.minecraft.mineinabyss.Layer.Layer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

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

            if (data.isJustChangedArea()) {
                data.setJustChangedArea(false);
                return;
            }


            // Admins are immune to effects by default
            if (player.hasPermission("mineinabyss.effectable")) {
                data.changeDistanceMovedUp(deltaY);

                if (data.getDistanceMovedUp() >= currentLayer.getOffset()) {
                    data.setDistanceMovedUp(data.getDistanceMovedUp() % distanceForEffect);
                    currentLayer.getEffectsOnLayer().forEach(effectBuilder -> {
                        data.applyEffect(effectBuilder.build());
                    });
                }
            }

            int currentSection = data.getCurrentSection();

            if (playerMoveEvent.getTo().getY() <= 10 && playerMoveEvent.getFrom().getY() > 10 && !currentLayer.isLastSection(currentSection)) {
                context.getLogger().info(player.getDisplayName() + " descending to next section");

                Vector nextSectionPoint = currentLayer.getSections().get(currentSection + 1);
                Vector currentSectionPoint = currentLayer.getSections().get(currentSection);

                Location tpLoc = player.getLocation().toVector().setY(230).add(nextSectionPoint).subtract(currentSectionPoint).toLocation(player.getWorld());
                tpLoc.setDirection(player.getLocation().getDirection());

                //TODO DONT HARDCODE U IDIOT
                player.teleport(tpLoc);
                data.setCurrentSection(currentSection + 1);
                data.setJustChangedArea(true);
            } else if (playerMoveEvent.getTo().getY() > 246 && playerMoveEvent.getFrom().getY() <= 246 && !currentLayer.isFirstSection(currentSection)) {
                context.getLogger().info(player.getDisplayName() + " ascending to next section");

                Vector nextSectionPoint = currentLayer.getSections().get(currentSection - 1);
                Vector currentSectionPoint = currentLayer.getSections().get(currentSection);

                Location tpLoc = player.getLocation().toVector().setY(26).add(nextSectionPoint).subtract(currentSectionPoint).toLocation(player.getWorld());
                tpLoc.setDirection(player.getLocation().getDirection());

                player.teleport(tpLoc);
                data.setCurrentSection(currentSection - 1);
                data.setJustChangedArea(true);
            }
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

        deadPlayerData.setCurrentSection(0);
        deadPlayerData.clearEffects(playerDeathEvent.getEntity());
        deadPlayerData.setDistanceMovedUp(0);
    }
}
