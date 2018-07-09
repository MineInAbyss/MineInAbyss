package com.derongan.minecraft.mineinabyss.Ascension;

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent;
import com.derongan.minecraft.deeperworld.event.PlayerChangeSectionEvent;
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent;
import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Player.PlayerData;
import com.derongan.minecraft.mineinabyss.World.AbyssWorldManager;
import com.derongan.minecraft.mineinabyss.World.Layer;
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

    public AscensionListener(AbyssContext context) {
        this.context = context;

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

        if(playerData.isAffectedByCurse()){
            double dist = playerData.getDistanceAscended();
            playerData.setDistanceAscended(Math.max(dist + changeY,0));

            if(dist >= 10){
                playerData.getCurrentLayer().getAscensionEffects().forEach(a->{
                   a.build().applyEffect(player,10);
                });
                playerData.setDistanceAscended(0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerChangeSection(PlayerChangeSectionEvent changeSectionEvent){
        if(!context.getPlayerDataMap().get(changeSectionEvent.getPlayer().getUniqueId()).isAnchored()){
            recentlyMovedPlayers.add(changeSectionEvent.getPlayer().getUniqueId());
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
