package com.mineinabyss.features.huds

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.huds.ReturnVanillaHud
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.features.helpers.changeHudState
import com.mineinabyss.features.helpers.changeHudStates
import com.mineinabyss.features.helpers.happyHUD
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.mineinabyss.core.abyss
import com.mineinabyss.mineinabyss.core.layer
import dev.geco.gsit.api.event.EntitySitEvent
import io.papermc.paper.event.entity.EntityInsideBlockEvent
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.potion.PotionEffectType
import org.spigotmc.event.entity.EntityDismountEvent
import org.spigotmc.event.entity.EntityMountEvent

class HudListener(private val feature: HudFeature) : Listener {
    // Remove all default meters when in creative or spectator
    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerJoinEvent.onJoin() {
        when (player.gameMode) {
            GameMode.CREATIVE, GameMode.SPECTATOR -> player.changeHudStates(
                happyHUD.layouts().defaults.map { it.key },
                false
            )

            GameMode.SURVIVAL, GameMode.ADVENTURE -> player.changeHudStates(
                happyHUD.layouts().defaults.map { it.key },
                true
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerAscendEvent.toggleHudOnAscend() {
        player.handleLayerHud(fromSection, toSection)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerDescendEvent.toggleHudOnDescend() {
        player.handleLayerHud(fromSection, toSection)
    }

    private val layerLayouts = listOf(
        feature.orthLayout,
        feature.edgeLayout,
        feature.forestLayout,
        feature.greatFaultLayout,
        feature.gobletsLayout,
        feature.seaLayout
    )

    // Handle displaying the layer name in the hud
    private fun Player.handleLayerHud(fromSection: Section, toSection: Section) {
        if (fromSection.layer == toSection.layer) return
        changeHudStates(layerLayouts, false) //Clear Layer Hud
        val layout = abyss.config.layers
            .firstOrNull { it == toSection.layer && it.name in layerLayouts }?.name ?: return
        changeHudState(layout, true)
    }

    // Remove all default meters when in creative or spectator
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerGameModeChangeEvent.onChangeGameMode() {
        when (newGameMode) {
            GameMode.CREATIVE, GameMode.SPECTATOR -> {
                player.changeHudStates(happyHUD.layouts().defaults.map { it.key }, false)
            }

            GameMode.SURVIVAL, GameMode.ADVENTURE -> {
                player.changeHudStates(happyHUD.layouts().defaults.map { it.key }, true)
            }
        }
    }

    @EventHandler
    fun InventoryClickEvent.onSwap() {
        val player = whoClicked as? Player ?: return
        if (slot != 40) return // Offhand slot
        player.swapBalanceHud()
    }

    @EventHandler
    fun PlayerSwapHandItemsEvent.onSwap() {
        player.swapBalanceHud()
    }

    private fun Player.swapBalanceHud() {
        when (inventory.itemInOffHand.type) {
            Material.AIR -> {
                changeHudState(feature.balanceEmptyOffhandLayout, false)
                changeHudState(feature.balanceOffhandLayout, true)
            }

            else -> {
                changeHudState(feature.balanceOffhandLayout, false)
                changeHudState(feature.balanceEmptyOffhandLayout, true)
            }
        }
    }

    /*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun ModelMountEvent.onMountModelEngine() {
        val player = (passenger as? Player) ?: return
        if (player.toGeary().has<ReturnVanillaHud>()) return
        player.changeHudState(feature.mountedLayout, true)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun ModelDismountEvent.onDismountModelEngine() {
        (passenger as? Player)?.changeHudState(feature.mountedLayout, false)
    }*/

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityMountEvent.onMount() {
        val player = entity as? Player ?: return
        if (player.toGeary().has<ReturnVanillaHud>()) return
        player.sendActionBar(Component.empty()) // Remove the server-sent mount action bar
        player.changeHudState(feature.mountedLayout, true)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityDismountEvent.onDismount() {
        (entity as? Player)?.changeHudState(feature.mountedLayout, false)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntitySitEvent.onSit() {
        val player = entity as? Player ?: return
        if (player.toGeary().has<ReturnVanillaHud>()) return
        player.sendActionBar(Component.empty()) // Remove the server-sent mount action bar
        player.changeHudState(feature.mountedLayout, false)
    }

    private val playerFrozenMap = mutableSetOf<Player>()

    // Add overlay frozen hud while player is frozen
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityInsideBlockEvent.onPowderSnow() {
        val player = entity as? Player ?: return
        if (player.toGeary().has<ReturnVanillaHud>()) return
        if (block.type != Material.POWDER_SNOW || !player.isValidGamemode()) return
        if (!player.isFrozen || player in playerFrozenMap) return

        abyss.plugin.launch(abyss.plugin.asyncDispatcher) {
            playerFrozenMap += player
            player.changeHudState(feature.freezingLayout, true)
            do {
                delay(4.ticks)
            } while (player.freezeTicks > 0)
            player.changeHudState(feature.freezingLayout, false)
            playerFrozenMap -= player
        }
    }

    // Apply huds if player relogs with effects or is in a lingering cloud
    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerJoinEvent.onJoinWithEffects() {
        if (player.toGeary().has<ReturnVanillaHud>()) return
        player.activePotionEffects.map { it.type }.forEach {
            player.handleEffectOverlays(it, true)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityPotionEffectEvent.onEffect() {
        val player = entity as? Player ?: return
        if (player.toGeary().has<ReturnVanillaHud>()) return
        when (action) {
            EntityPotionEffectEvent.Action.ADDED, EntityPotionEffectEvent.Action.CHANGED ->
                player.handleEffectOverlays(newEffect?.type ?: return, true)

            EntityPotionEffectEvent.Action.REMOVED, EntityPotionEffectEvent.Action.CLEARED ->
                player.handleEffectOverlays(oldEffect?.type ?: return, false)
        }
    }

    private fun Player.handleEffectOverlays(effect: PotionEffectType, toggle: Boolean) {
        if (!this.isValidGamemode()) return
        when (effect) {
            PotionEffectType.ABSORPTION -> changeHudState(feature.absorptionLayout, toggle)
            PotionEffectType.HUNGER -> changeHudState(feature.hungerLayout, toggle)
            PotionEffectType.WITHER -> changeHudState(feature.bleedingLayout, toggle)
            PotionEffectType.POISON -> changeHudState(feature.poisonLayout, toggle)
        }
    }

    private fun Player.isValidGamemode() = gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE
}
