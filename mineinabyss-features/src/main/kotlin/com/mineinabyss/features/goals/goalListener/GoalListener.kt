package com.mineinabyss.features.goals.goalListener

import com.mineinabyss.features.goals.FactKind
import com.mineinabyss.features.goals.GoalsConfig
import com.mineinabyss.features.goals.ItemCategories
import com.mineinabyss.features.goals.repository.GoalRepository
import com.mineinabyss.idofront.plugin.Services
import com.mineinabyss.geary.papermc.spawning.locations.PlayerEnterRegionEvent
import com.mineinabyss.geary.papermc.spawning.locations.RegionService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class GoalListener(
    private val repository: GoalRepository,
    config: GoalsConfig,
) : Listener {
    private val categories = ItemCategories(config.itemCategories)

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

    @EventHandler(ignoreCancelled = true)
    fun CraftItemEvent.onCraft() {
        val player = whoClicked as? Player ?: return
        recipe.result.itemFactIds(player.world).forEach {
            repository.recordFact(player, FactKind.CRAFT, it)
            repository.recordFact(player, FactKind.CRAFT_ANY, it)
        }
    }

    @EventHandler
    fun EntityDeathEvent.onKill() {
        val killer = entity.killer ?: return
        entity.killFactIds().forEach { repository.recordFact(killer, FactKind.KILL, it) }
    }

    @EventHandler
    fun EntityPickupItemEvent.onPickup() {
        val player = entity as? Player ?: return
        val factIds = item.itemStack.itemFactIds(player.world)
        factIds.forEach { repository.recordFact(player, FactKind.PICKUP, it, item.itemStack.amount) }
        categories.categoriesOf(factIds).forEach {
            repository.recordFact(player, FactKind.CATEGORY_PICKUP, it, item.itemStack.amount)
        }
    }

    @EventHandler
    fun PlayerShootProjectileEvent.onShoot() {
        weapon?.let { repository.recordFactAny(player, FactKind.WEAPON_SHOOT, it.itemFactIds(player.world)) }
        projectile?.let { repository.recordFactAny(player, FactKind.PROJECTILE_SHOOT, it.itemFactIds(player.world)) }
    }

    @EventHandler
    fun PlayerProjectileDamageEvent.onProjectileDamage() {
        weapon?.let { repository.recordFactAny(player, FactKind.WEAPON_HIT, it.itemFactIds(player.world)) }
        projectile?.let { repository.recordFactAny(player, FactKind.PROJECTILE_HIT, it.itemFactIds(player.world)) }
        repository.recordFactAny(player, FactKind.ENTITY_HIT, entityHitFactIds(weapon ?: projectile, victim, player.world))
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onMeleeDamage() {
        val player = damager as? Player ?: return
        val weapon = player.inventory.itemInMainHand
        repository.recordFactAny(player, FactKind.ENTITY_HIT, entityHitFactIds(weapon, entity, player.world))
    }
}
