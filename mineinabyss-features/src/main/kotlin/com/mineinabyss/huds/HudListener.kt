package com.mineinabyss.huds

import com.ehhthan.happyhud.api.event.PlayerUpdateAttributeEvent
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.huds.AlwaysShowAirHud
import com.mineinabyss.components.huds.AlwaysShowArmorHud
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.changeHudState
import com.mineinabyss.helpers.changeHudStates
import com.mineinabyss.helpers.happyHUD
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.ticxo.modelengine.api.events.ModelDismountEvent
import com.ticxo.modelengine.api.events.ModelMountEvent
import dev.geco.gsit.api.event.EntitySitEvent
import io.papermc.paper.event.entity.EntityInsideBlockEvent
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityAirChangeEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffectType
import org.spigotmc.event.entity.EntityDismountEvent
import org.spigotmc.event.entity.EntityMountEvent

class HudListener(private val feature: HudFeature) : Listener {
    // Remove all default meters when in creative or spectator
    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerJoinEvent.onJoin() {
        when (player.gameMode) {
            GameMode.CREATIVE, GameMode.SPECTATOR -> player.changeHudStates(happyHUD.layouts().defaults.map { it.key }, false)
            GameMode.SURVIVAL, GameMode.ADVENTURE -> player.changeHudStates(happyHUD.layouts().defaults.map { it.key }, true)
        }

        // Toggle armor and air if they are set to always show
        // TODO Make these components persist on players
        player.changeHudState(feature.airLayout, player.toGeary().has<AlwaysShowAirHud>())
        player.changeHudState(feature.armorLayout, player.toGeary().has<AlwaysShowArmorHud>())
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
        val layout = MIAConfig.data.layers.firstOrNull { it == toSection.layer }?.name ?: return
        changeHudState(layout, true)
    }

    // Remove all default meters when in creative or spectator
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerGameModeChangeEvent.onChangeGameMode() {
        when (newGameMode) {
            GameMode.CREATIVE, GameMode.SPECTATOR -> {
                player.changeHudStates(happyHUD.layouts().defaults.map { it.key }, false)
                player.changeHudState(feature.armorLayout, false)
            }
            GameMode.SURVIVAL, GameMode.ADVENTURE -> {
                player.changeHudStates(happyHUD.layouts().defaults.map { it.key }, true)
                if (player.displayArmorHud())
                    player.changeHudState(feature.armorLayout, true)
            }
        }
    }

    @EventHandler
    fun EntityAirChangeEvent.onPlayerUnderwater() {
        val player = entity as? Player ?: return
        if (player.toGeary().has<AlwaysShowAirHud>()) return
        // Because it is ticks, offset it abit to register when it is max
        player.changeHudState(feature.airLayout, player.remainingAir < player.maximumAir - 4)
    }

    @EventHandler
    fun PlayerUpdateAttributeEvent.onArmorChange() {
        if (attribute != Attribute.GENERIC_ARMOR) return
        if (player.toGeary().has<AlwaysShowArmorHud>()) return
        player.changeHudState(feature.armorLayout, (player.displayArmorHud()))
    }

    private fun Player.displayArmorHud() : Boolean {
        val attribute = getAttribute(Attribute.GENERIC_ARMOR) ?: return false
        return attribute.value > attribute.baseValue && isValidGamemode()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun ModelMountEvent.onMountModelEngine() {
        (passenger as? Player)?.changeHudState(feature.mountedLayout, true)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun ModelDismountEvent.onDismountModelEngine() {
        (passenger as? Player)?.changeHudState(feature.mountedLayout, false)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityMountEvent.onMount() {
        val player = entity as? Player ?: return
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
        player.sendActionBar(Component.empty()) // Remove the server-sent mount action bar
        player.changeHudState(feature.mountedLayout, false)
    }

    private val playerFrozenMap = mutableSetOf<Player>()

    // Add overlay frozen hud while player is frozen
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityInsideBlockEvent.onPowderSnow() {
        val player = entity as? Player ?: return
        if (block.type != Material.POWDER_SNOW || !player.isValidGamemode()) return
        if (!player.isFrozen || player in playerFrozenMap) return

        mineInAbyss.launch(mineInAbyss.asyncDispatcher) {
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
        player.activePotionEffects.map { it.type }.forEach {
            player.handleEffectOverlays(it, true)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun EntityPotionEffectEvent.onEffect() {
        val player = entity as? Player ?: return
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
            PotionEffectType.HUNGER ->  changeHudState(feature.hungerLayout, toggle)
            PotionEffectType.WITHER -> changeHudState(feature.bleedingLayout, toggle)
            PotionEffectType.POISON -> changeHudState(feature.poisonLayout, toggle)
        }
    }

    private fun Player.isValidGamemode() = gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE
}
