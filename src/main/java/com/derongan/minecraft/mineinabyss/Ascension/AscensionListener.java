package com.derongan.minecraft.mineinabyss.Ascension;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects.DeathAscensionEffect;
import com.derongan.minecraft.mineinabyss.Layer.Layer;
import com.derongan.minecraft.mineinabyss.Layer.Section;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
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

            if(data.isDev()){
                return;
            }

            double toY = playerMoveEvent.getTo().getY();
            double fromY = playerMoveEvent.getFrom().getY();

            if (currentLayer.getSections().size() > 0) {
                int currentSectionNum = data.getCurrentSection();
                Section currentSection = currentLayer.getSections().get(currentSectionNum);

                int shared;

                if (!currentLayer.isLastSection(currentSectionNum)) {
                    Section nextSection = currentLayer.getSections().get(currentSectionNum + 1);

                    shared = currentSection.getSharedWithBelow();

                    // once you are three quarters of the distance into the shared area, teleport
                    int threshold = (int) (shared * .4);
                    int invThresh = shared - threshold;

                    if (toY <= threshold && fromY > toY) {
                        context.getLogger().info(player.getDisplayName() + " descending to next section");

                        Vector nextSectionPoint = nextSection.getOffset();
                        Vector currentSectionPoint = currentSection.getOffset();

                        Location tpLoc = player.getLocation().toVector().setY(256 - invThresh).add(nextSectionPoint).subtract(currentSectionPoint).toLocation(player.getWorld());
                        tpLoc.setDirection(player.getLocation().getDirection());

                        player.teleport(tpLoc);
                        data.setCurrentSection(currentSectionNum + 1);
                        data.setJustChangedArea(true);
                    }
                }
                if (!currentLayer.isFirstSection(currentSectionNum)) {
                    Section nextSection = currentLayer.getSections().get(currentSectionNum - 1);

                    shared = nextSection.getSharedWithBelow();

                    // once you are one quarters of the distance from the top of the shared area, teleport
                    int threshold = (int) (shared * .4);
                    int invThresh = shared - threshold;
                    ;

                    if (toY >= 256 - threshold && fromY < toY) {
                        context.getLogger().info(player.getDisplayName() + " ascending to next section");

                        Vector nextSectionPoint = nextSection.getOffset();
                        Vector currentSectionPoint = currentSection.getOffset();

                        Location tpLoc = player.getLocation().toVector().setY(invThresh).add(nextSectionPoint).subtract(currentSectionPoint).toLocation(player.getWorld());
                        tpLoc.setDirection(player.getLocation().getDirection());

                        player.teleport(tpLoc);
                        data.setCurrentSection(currentSectionNum - 1);
                        data.setJustChangedArea(true);
                    }
                }
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

        Player player = playerDeathEvent.getEntity();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(2);
    }
}
