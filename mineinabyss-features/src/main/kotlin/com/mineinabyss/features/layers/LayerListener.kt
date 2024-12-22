package com.mineinabyss.features.layers

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerChangeSectionEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.deeperworld.services.PlayerManager
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.eternalfortune.api.events.PlayerCreateGraveEvent
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.layer
import com.mineinabyss.features.hubstorage.isInHub
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.features.common.cooldowns.Cooldown
import com.mineinabyss.geary.papermc.features.common.cooldowns.Cooldowns
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket
import net.minecraft.world.level.border.WorldBorder
import org.bukkit.Bukkit
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class LayerListener : Listener {

    init {
        if (abyss.isEternalFortuneLoaded) Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            fun PlayerCreateGraveEvent.onCreateGrave() {
                if (player.isInHub()) isCancelled = true
            }
        }, abyss.plugin)
    }

    private val titleCooldown = Cooldown(2.seconds, null, "mineinabyss:layer_title")

    @EventHandler
    fun PlayerAscendEvent.onPlayerAscend() { sendTitleOnLayerChange() }

    @EventHandler(ignoreCancelled = true)
    fun PlayerDescendEvent.onPlayerDescend() { sendTitleOnLayerChange() }

    private fun PlayerChangeSectionEvent.sendTitleOnLayerChange() {
        if (!PlayerManager.playerCanTeleport(player)) return
        val gearyPlayer = player.toGearyOrNull() ?: return
        if (!Cooldowns.isComplete(gearyPlayer, "mineinabyss:layer_title")) return
        val fromLayer = fromSection.layer ?: return
        val toLayer = toSection.layer.takeUnless { it == fromLayer } ?: return
        val times = Title.Times.times(2.5.seconds.toJavaDuration(), 0.5.seconds.toJavaDuration(), 1.seconds.toJavaDuration())

        player.showTitle(Title.title(toLayer.name.miniMsg(), toLayer.sub.miniMsg(), times))
        titleCooldown.execute(ActionGroupContext(gearyPlayer))
    }

    @EventHandler
    fun PlayerAscendEvent.onPlayerChangeSection() { player.sendWorldBorderPackets(toSection) }
    @EventHandler
    fun PlayerDescendEvent.onPlayerChangeSection() { player.sendWorldBorderPackets(toSection) }

    @EventHandler
    fun PlayerTeleportEvent.onPlayerTeleport() { (to.section ?: from.section)?.let { player.sendWorldBorderPackets(it) } }
    @EventHandler
    fun PlayerPostRespawnEvent.onPlayerRespawn() { respawnedLocation.section?.let { player.sendWorldBorderPackets(it) } }
    @EventHandler
    fun PlayerJoinEvent.onPlayerJoin() { player.location.section?.let { player.sendWorldBorderPackets(it) } }

    private fun Player.sendWorldBorderPackets(section: Section) {
        val settings = WorldBorder()
        settings.setCenter(section.centerLocation.x, section.centerLocation.z)
        settings.size = (section.region.max.x - section.region.min.x).toDouble() + 2.0

        val connection = (player as CraftPlayer).handle.connection
        connection.send(ClientboundSetBorderCenterPacket(settings))
        connection.send(ClientboundSetBorderSizePacket(settings))
    }

    @EventHandler
    fun PlayerDeathEvent.appendLayerToDeathMessage() {
        val layerOfDeath =  player.location.section?.layer ?: return
        deathMessage(deathMessage()?.let { Component.textOfChildren(it, Component.space(), layerOfDeath.deathMessage) })
    }

    @EventHandler
    fun FoodLevelChangeEvent.onFoodChange() {
        val player = entity as? Player ?: return
        if (player.isInHub() && player.foodLevel > foodLevel) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockFromToEvent.onLiquidFlow() {
        val liquidFlowLimit = block.location.section?.layer?.liquidFlowLimit?.takeIf { it > -1 } ?: return
        var height = 0
        while (block.getRelative(BlockFace.UP, height).type == block.type) height++
        if (height >= liquidFlowLimit) isCancelled = true
    }
}
