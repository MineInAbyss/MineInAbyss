package com.derongan.minecraft.mineinabyss.player;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

public class PlayerListener implements Listener{
    private AbyssContext context;
    private PlayerDataConfigManager playerDataConfigManager;

    public PlayerListener(AbyssContext context) {
        this.context = context;
        this.playerDataConfigManager = new PlayerDataConfigManager(context);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        context.getPlayerDataMap().put(
                joinEvent.getPlayer().getUniqueId(),
                playerDataConfigManager.loadPlayerData(joinEvent.getPlayer())
        );
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent playerQuitEvent) {
        PlayerData data = context.getPlayerDataMap().remove(playerQuitEvent.getPlayer().getUniqueId());
        try {
            playerDataConfigManager.savePlayerData(data);
        } catch (IOException e) {
            context.getLogger().warning("Failed to save data for player "+playerQuitEvent.getPlayer().getUniqueId().toString());
            e.printStackTrace();
        }
    }
}
