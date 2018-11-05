package com.derongan.minecraft.mineinabyss.ascension;

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent;
import com.derongan.minecraft.deeperworld.event.PlayerChangeSectionEvent;
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent;
import com.derongan.minecraft.deeperworld.world.WorldManager;
import com.derongan.minecraft.deeperworld.world.section.Section;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.player.PlayerData;
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManager;
import com.derongan.minecraft.mineinabyss.world.Layer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AscensionListener implements Listener {
    private AbyssContext context;
    private Set<UUID> recentlyMovedPlayers;

    private WorldManager worldManager;

    public AscensionListener(AbyssContext context) {
        this.context = context;

        worldManager = Bukkit.getServicesManager().load(WorldManager.class);
        recentlyMovedPlayers = new HashSet<>();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        Player player = moveEvent.getPlayer();

        if (recentlyMovedPlayers.contains(player.getUniqueId())) {
            recentlyMovedPlayers.remove(player.getUniqueId());
            return;
        }

        AbyssWorldManager manager = context.getWorldManager();

        if (!manager.isAbyssWorld(player.getWorld()))
            return;

        Location from = moveEvent.getFrom();
        Location to = moveEvent.getTo();

        double changeY = to.getY() - from.getY();

        PlayerData playerData = context.getPlayerDataMap().get(player.getUniqueId());

        if (playerData.isAffectedByCurse()) {
            double dist = playerData.getDistanceAscended();
            playerData.setDistanceAscended(Math.max(dist + changeY, 0));

            if (dist >= 10) {
                Layer layerForSection = manager.getLayerForSection(worldManager.getSectionFor(moveEvent.getFrom()));

                if (layerForSection != null) {
                    layerForSection.getAscensionEffects().forEach(a -> {
                        a.build().applyEffect(player, 10);
                    });
                    playerData.setDistanceAscended(0);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerAscend(PlayerAscendEvent e){
        onPlayerChangeSection(e);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerDescend(PlayerDescendEvent e){
        onPlayerChangeSection(e);
    }

    private void onPlayerChangeSection(PlayerChangeSectionEvent changeSectionEvent) {
        if (!context.getPlayerDataMap().get(changeSectionEvent.getPlayer().getUniqueId()).isAnchored()) {
            recentlyMovedPlayers.add(changeSectionEvent.getPlayer().getUniqueId());
            AbyssWorldManager manager = context.getWorldManager();

            Section fromSection = changeSectionEvent.getFromSection();
            Section toSection = changeSectionEvent.getToSection();

            Layer fromLayer = manager.getLayerForSection(fromSection);
            Layer toLayer = manager.getLayerForSection(toSection);

            if (fromLayer != toLayer) {
                changeSectionEvent.getPlayer().sendTitle(toLayer.getName(), toLayer.getSub(), 50, 10, 20);
            }

        } else {
            changeSectionEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent deathEvent) {
        Player player = deathEvent.getEntity();

        // Catching for bad stuff
        if (player == null)
            return;

        AbyssWorldManager manager = context.getWorldManager();

        Layer layerOfDeath = context.getPlayerDataMap().get(player.getUniqueId()).getCurrentLayer();

        deathEvent.setDeathMessage(deathEvent.getDeathMessage() + layerOfDeath.getDeathMessage());
    }
}
