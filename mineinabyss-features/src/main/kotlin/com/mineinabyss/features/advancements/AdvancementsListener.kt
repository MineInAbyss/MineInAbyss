package com.mineinabyss.features.advancements

import com.mineinabyss.features.helpers.di.Features.advancements
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class AdvancementsListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        advancements.advancementManager.apply {
            loadProgress(player)
            addPlayer(player)
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        advancements.advancementManager.apply {
            saveProgress(player)
            //removePlayer(player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreak() {
        if (block.type != Material.NOTE_BLOCK) return
        //player.grantAdvancement("charcoal_sand_ore")
    }
}
