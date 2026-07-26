package com.mineinabyss.features.achievements

import com.mineinabyss.features.goals.FactKind
import com.mineinabyss.features.goals.goalListener.itemFactIds
import com.mineinabyss.features.goals.goalListener.killFactIds
import com.mineinabyss.geary.papermc.spawning.locations.PlayerEnterRegionEvent
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class AchievementListener(
    private val manager: AchievementManager,
) : Listener {
    @EventHandler
    suspend fun PlayerJoinEvent.onJoin() {
        manager.repository.loadPlayer(player)
        manager.autoStart(player)
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() = manager.repository.unloadPlayer(player)

    @EventHandler
    fun PlayerEnterRegionEvent.onRegionEntered() =
        manager.repository.recordFact(player, FactKind.REGION_ENTER, regionId)

    @EventHandler(ignoreCancelled = true)
    fun CraftItemEvent.onCraft() {
        val player = whoClicked as? Player ?: return
        recipe.result.itemFactIds(player.world).forEach { manager.repository.recordFact(player, FactKind.CRAFT, it) }
    }

    @EventHandler
    fun EntityDeathEvent.onKill() {
        val killer = entity.killer ?: return
        entity.killFactIds().forEach { manager.repository.recordFact(killer, FactKind.KILL, it) }
    }

    @EventHandler
    fun EntityPickupItemEvent.onPickup() {
        val player = entity as? Player ?: return
        item.itemStack.itemFactIds(player.world).forEach { manager.repository.recordFact(player, FactKind.PICKUP, it, item.itemStack.amount) }
    }
}
