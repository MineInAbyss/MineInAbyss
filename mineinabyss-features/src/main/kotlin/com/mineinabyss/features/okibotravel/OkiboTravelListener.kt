package com.mineinabyss.features.okibotravel

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.blocky.api.events.furniture.BlockyFurnitureBreakEvent
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.components.playerData
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.di.Features.okiboLine
import com.mineinabyss.features.hubstorage.isInHub
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.papermc.features.common.cooldowns.Cooldowns
import com.mineinabyss.geary.papermc.features.common.cooldowns.StartCooldown
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import kotlin.time.Duration.Companion.seconds

class OkiboTravelListener : Listener {

    private val okiboMapCooldown = StartCooldown(1.seconds, null, "mineinabyss:okibomap")

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onInteractMap() {
        val gearyPlayer = player.toGeary().takeIf { hand == EquipmentSlot.HAND } ?: return
        if (!Cooldowns.isComplete(gearyPlayer, okiboMapCooldown.id)) return
        okiboMapCooldown.execute(ActionGroupContext(gearyPlayer))
        val destination = getHitboxStation(entityId)?.getStation ?: return
        val playerStation = okiboLine.config.allStations.filter { it != destination }.minByOrNull { it.location.distanceSquared(player.location) } ?: return player.error("You are not near a station!")
        val cost = playerStation.costTo(destination) ?: return player.error("You cannot travel to that station!")

        gearyPlayer.with { traveler: OkiboTraveler ->
            when (traveler.selectedDestination) {
                destination -> {
                    when {
                        cost > player.playerData.orthCoinsHeld -> player.error("You do not have enough coins to travel to that station!")
                        cost == 0 -> player.error("You are already at that station!")
                        else -> {
                            player.playerData.orthCoinsHeld -= cost
                            spawnOkiboCart(player, playerStation, destination)
                        }
                    }
                    return
                }
                else -> player.toGeary().remove<OkiboTraveler>()
            }
        }

        player.info("<gold>You selected <yellow>${destination.name}</yellow> station!")
        player.info("<gold>The cost to travel there will be <yellow>$cost</yellow> Orth Coins.")
        player.info("<gold>Click the map again to confirm your selection.")
        gearyPlayer.set(OkiboTraveler(destination))

        abyss.plugin.launch {
            delay(5.seconds)
            if (player.isOnline) gearyPlayer.remove<OkiboTraveler>()
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.toGeary().remove<OkiboTraveler>()
        abyss.plugin.launch {
            delay(1.seconds)
            player.sendOkiboMaps()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockyFurnitureBreakEvent.onBreakNoticeBoard() {
        val prefabKey = entity.toGearyOrNull()?.prefabs?.firstOrNull()?.get<PrefabKey>() ?: return
        if (!entity.isInHub() || prefabKey !in okiboLine.config.okiboMaps.mapNotNull { it.noticeBoardFurniture?.prefabKey }) return

        isCancelled = true
    }
}
