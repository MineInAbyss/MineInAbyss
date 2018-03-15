package com.derongan.minecraft.mineinabyss.plugin.Ascension;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Ascension.Effect.Effects.DeathAscensionEffect;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Layer;
import com.derongan.minecraft.mineinabyss.plugin.Layer.Section;
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

    @EventHandler()
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        Player player = playerMoveEvent.getPlayer();

        Layer currentLayer = context.getLayerMap().getOrDefault(player.getWorld().getName(), null);

        if (currentLayer != null) {
            AscensionData data = context.getPlayerAcensionDataMap().computeIfAbsent(player.getUniqueId(), uuid -> new AscensionData(player));

            if (isPlayerExempt(data)) {
                return;
            }

            double deltaY = playerMoveEvent.getTo().getY() - playerMoveEvent.getFrom().getY();
            doAscensionEffect(deltaY, currentLayer, data);

            double toY = playerMoveEvent.getTo().getY();
            double fromY = playerMoveEvent.getFrom().getY();

            if (currentLayer.getSections().size() > 0) {
                int currentSectionNum = data.getCurrentSection();
                Section currentSection = currentLayer.getSections().get(currentSectionNum);

                doDescend(player, currentLayer, data, toY, fromY, currentSectionNum, currentSection);
                doAscend(player, currentLayer, data, toY, fromY, currentSectionNum, currentSection);
            }
        }
    }

    private void doDescend(Player player, Layer currentLayer, AscensionData data, double toY, double fromY, int currentSectionNum, Section currentSection) {
        int shared;
        Layer nextLayer = currentLayer.getNextLayer();
        if (!(currentLayer.isLastSection(currentSectionNum)) || (nextLayer != null && nextLayer.getSections().size() > 0)) {
            Section nextSection;
            if (currentLayer.isLastSection(currentSectionNum))
                nextSection = nextLayer.getSections().get(0);
            else
                nextSection = currentLayer.getSections().get(currentSectionNum + 1);

            if(nextSection.getWorld() == null){
                return;
            }

            shared = currentSection.getSharedWithBelow();

            // once you are three quarters of the distance into the shared area, teleport
            double threshold = (shared * .4);
            double invThresh = shared * .6;

            if (toY <= threshold && fromY > toY) {
                context.getLogger().info(player.getDisplayName() + " descending to next section");

                Vector nextSectionPoint = nextSection.getOffset();
                Vector currentSectionPoint = currentSection.getOffset();

                Location tpLoc = player.getLocation().toVector().setY(256 - invThresh).add(nextSectionPoint).subtract(currentSectionPoint).toLocation(nextSection.getWorld());
                tpLoc.setDirection(player.getLocation().getDirection());

                player.teleport(tpLoc);

                if (currentLayer.isLastSection(currentSectionNum))
                    data.setCurrentSection(0);
                else
                    data.setCurrentSection(currentSectionNum + 1);

                data.setJustChangedArea(true);
            }
        }
    }

    private void doAscend(Player player, Layer currentLayer, AscensionData data, double toY, double fromY, int currentSectionNum, Section currentSection) {
        int shared;
        Layer prevLayer = currentLayer.getPrevLayer();
        if (!(currentLayer.isFirstSection(currentSectionNum)) || (prevLayer != null && prevLayer.getSections().size() > 0)) {
            Section nextSection;
            if (currentLayer.isFirstSection(currentSectionNum))
                nextSection = prevLayer.getSections().get(prevLayer.getSections().size() - 1);
            else
                nextSection = currentLayer.getSections().get(currentSectionNum - 1);

            if(nextSection.getWorld() == null){
                return;
            }

            shared = nextSection.getSharedWithBelow();

            // once you are one quarters of the distance from the top of the shared area, teleport
            double threshold = (shared * .4);
            double invThresh = shared * .6;

            if (toY >= 256 - threshold && fromY < toY) {
                context.getLogger().info(player.getDisplayName() + " ascending to next section");

                Vector nextSectionPoint = nextSection.getOffset();
                Vector currentSectionPoint = currentSection.getOffset();

                Location tpLoc = player.getLocation().toVector().setY(invThresh).add(nextSectionPoint).subtract(currentSectionPoint).toLocation(nextSection.getWorld());
                tpLoc.setDirection(player.getLocation().getDirection());

                player.teleport(tpLoc);

                if (currentLayer.isFirstSection(currentSectionNum))
                    data.setCurrentSection(prevLayer.getSections().size() - 1);
                else
                    data.setCurrentSection(currentSectionNum - 1);

                data.setJustChangedArea(true);
            }
        }
    }


    /**
     * Return if the player has modifiers making them exempt from tp/effects
     *
     * @param data the AscensionData for the player
     * @return true if the player is exempt
     */
    private boolean isPlayerExempt(AscensionData data) {
        if (data.isJustChangedArea()) {
            data.setJustChangedArea(false);
            return true;
        }
        return data.isDev();
    }

    private void doAscensionEffect(double deltaY, Layer currentLayer, AscensionData data) {
        Player player = data.getPlayer();

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
    }


    //TODO can I get a better cause reason
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        AscensionData deadPlayerData = context.getPlayerAcensionDataMap().get(playerDeathEvent.getEntity().getUniqueId());
        String layerOfDeathName = playerDeathEvent.getEntity().getWorld().getName();
        String playerName = playerDeathEvent.getEntity().getName();
        Layer layerOfDeath = context.getLayerMap().getOrDefault(layerOfDeathName, null);

        if(layerOfDeath == null)
            return;

        //TODO improve logic
//        if (layerOfDeath.getDeathMessage() != null || deadPlayerData.getCurrentEffects().stream().anyMatch(a -> a instanceof DeathAscensionEffect) || (playerDeathEvent.getDeathMessage().contains("withered away"))) {
//            playerDeathEvent.setDeathMessage(layerOfDeath.getDeathMessage().replace("{Player}", playerName));
//        } else {
            playerDeathEvent.setDeathMessage(playerDeathEvent.getDeathMessage() + " in the depths of the abyss");
//        }

        deadPlayerData.setCurrentSection(0);
        deadPlayerData.clearEffects(playerDeathEvent.getEntity());
        deadPlayerData.setDistanceMovedUp(0);

        Player player = playerDeathEvent.getEntity();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(2);
    }
}
