package com.mineinabyss.features.quests

import com.mineinabyss.features.goals.FactKind
import com.mineinabyss.features.goals.goalListener.itemFactIds
import com.mineinabyss.features.goals.goalListener.killFactIds
import com.mineinabyss.geary.papermc.spawning.locations.PlayerEnterRegionEvent
import com.mineinabyss.geary.papermc.spawning.locations.RegionService
import com.mineinabyss.idofront.plugin.Services
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class QuestListener(
    private val manager: QuestManager,
) : Listener {
    private val repository get() = manager.repository

    @EventHandler
    suspend fun PlayerJoinEvent.onJoin() {
        repository.loadPlayer(player)
        val regions = Services.getOrNull<RegionService>()?.regionsAt(player.location) ?: return
        repository.seedRegions(player, regions)
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() = repository.unloadPlayer(player)

    @EventHandler
    fun PlayerEnterRegionEvent.onRegionEntered() =
        repository.recordFact(player, FactKind.REGION_ENTER, regionId)

    @EventHandler
    fun CraftItemEvent.onCraft() {
        val player = whoClicked as? Player ?: return
        recipe.result.itemFactIds(player.world).forEach { repository.recordFact(player, FactKind.CRAFT, it) }
    }

    @EventHandler
    fun EntityDeathEvent.onKill() {
        val killer = entity.killer ?: return
        entity.killFactIds().forEach { repository.recordFact(killer, FactKind.KILL, it) }
    }

    @EventHandler
    fun EntityPickupItemEvent.onPickup() {
        val player = entity as? Player ?: return
        item.itemStack.itemFactIds(player.world).forEach { repository.recordFact(player, FactKind.PICKUP, it, item.itemStack.amount) }
    }
}
