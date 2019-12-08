package com.derongan.minecraft.mineinabyss.player;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.commands.GUICommandExecutor;
import com.derongan.minecraft.mineinabyss.MineInAbyss;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

public class PlayerListener implements Listener {
    private AbyssContext context;
    private PlayerDataConfigManager playerDataConfigManager;

    public PlayerListener(AbyssContext context) {
        this.context = context;
        this.playerDataConfigManager = context.getConfigManager().getPlayerDataCM();
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
            context.getLogger().warning("Failed to save data for player " + playerQuitEvent.getPlayer().getUniqueId().toString());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent pde) {
        Player player = pde.getEntity();
        PlayerData playerData = MineInAbyss.getContext().getPlayerData(player);
        GUICommandExecutor.Companion.leave(playerData);
    }

    @EventHandler
    public void onPlayerGainEXP(PlayerExpChangeEvent pexpce) {
        int amount = pexpce.getAmount();
        if (amount <= 0)
            return;

        Player player = pexpce.getPlayer();
        MineInAbyss.getEcon().depositPlayer(player, amount);
        context.getPlayerData(player).addExp(amount);
    }
}
