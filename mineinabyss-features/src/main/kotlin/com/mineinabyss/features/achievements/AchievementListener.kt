package com.mineinabyss.features.achievements

import com.mineinabyss.features.goals.FactKind
import com.mineinabyss.features.goals.ItemCategories
import com.mineinabyss.features.goals.goalListener.PlayerProjectileDamageEvent
import com.mineinabyss.features.goals.goalListener.PlayerShootProjectileEvent
import com.mineinabyss.features.goals.goalListener.entityHitFactIds
import com.mineinabyss.features.goals.goalListener.itemFactIds
import com.mineinabyss.features.goals.goalListener.killFactIds
import com.mineinabyss.geary.papermc.spawning.locations.PlayerEnterRegionEvent
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.staminaclimb.events.PlayerStartClimbEvent
import com.mineinabyss.staminaclimb.events.PlayerStopClimbEvent
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class AchievementListener(
    private val manager: AchievementManager,
    config: AchievementsConfig,
) : Listener {
    private val categories = ItemCategories(config.itemCategories)

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
        recipe.result.itemFactIds(player.world).forEach {
            manager.repository.recordFact(player, FactKind.CRAFT, it)
            manager.repository.recordFact(player, FactKind.CRAFT_ANY, it)
        }
    }

    @EventHandler
    fun EntityDeathEvent.onKill() {
        val killer = entity.killer ?: return
        entity.killFactIds().forEach { manager.repository.recordFact(killer, FactKind.KILL, it) }
    }

    @EventHandler
    fun EntityPickupItemEvent.onPickup() {
        val player = entity as? Player ?: return
        val factIds = item.itemStack.itemFactIds(player.world)
        factIds.forEach { manager.repository.recordFact(player, FactKind.PICKUP, it, item.itemStack.amount) }
        categories.categoriesOf(factIds).forEach {
            manager.repository.recordFact(player, FactKind.CATEGORY_PICKUP, it, item.itemStack.amount)
        }
    }

    @EventHandler
    fun PlayerShootProjectileEvent.onShoot() {
        weapon?.let { manager.repository.recordFactAny(player, FactKind.WEAPON_SHOOT, it.itemFactIds(player.world)) }
        projectile?.let { manager.repository.recordFactAny(player, FactKind.PROJECTILE_SHOOT, it.itemFactIds(player.world)) }
    }

    @EventHandler
    fun PlayerProjectileDamageEvent.onProjectileDamage() {
        weapon?.let { manager.repository.recordFactAny(player, FactKind.WEAPON_HIT, it.itemFactIds(player.world)) }
        projectile?.let { manager.repository.recordFactAny(player, FactKind.PROJECTILE_HIT, it.itemFactIds(player.world)) }
        manager.repository.recordFactAny(player, FactKind.ENTITY_HIT, entityHitFactIds(weapon ?: projectile, victim, player.world))
    }

    @EventHandler(ignoreCancelled = true)
    fun EntityDamageByEntityEvent.onMeleeDamage() {
        val player = damager as? Player ?: return
        val weapon = player.inventory.itemInMainHand
        manager.repository.recordFactAny(player, FactKind.ENTITY_HIT, entityHitFactIds(weapon, entity, player.world))
    }

    @EventHandler
    fun PlayerStartClimbEvent.onClimb() {
        // on climb logic
    }

    @EventHandler
    fun PlayerStopClimbEvent.onStopClimb() {
        // on stop climb logic, can access climbedBlocks  as well as dist up and down
    }

}
