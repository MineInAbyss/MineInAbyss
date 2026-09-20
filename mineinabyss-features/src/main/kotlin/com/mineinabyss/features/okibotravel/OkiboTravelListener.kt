package com.mineinabyss.features.okibotravel

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.papermc.features.common.cooldowns.Cooldown
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import kotlin.time.Duration.Companion.seconds

/** How far a player may be from a board and still click its dots */
private const val MAX_BOARD_DISTANCE = 8.0

class OkiboTravelListener(
    val config: OkiboTravelConfig,
    val okibo: OkiboRepository,
) : Listener {
    private val okiboMapCooldown = Cooldown(1.seconds, null, "mineinabyss:okibomap")

    @EventHandler
    suspend fun PlayerChunkLoadEvent.onLoad() {
        delay(2.ticks)
        val okiboMap = okibo.mapAt(chunk) ?: return
        if (player.isOnline) okibo.sendMap(player, okiboMap)
    }

    @EventHandler
    fun PlayerChunkUnloadEvent.onUntrack() {
        val okiboMap = okibo.mapAt(chunk) ?: return
        okibo.removeMap(player, okiboMap)
    }

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onInteractMap() {
        if (hand != EquipmentSlot.HAND) return
        val (map, origin, destination) = okibo.target(entityId) ?: return

        val gearyPlayer = player.toGeary()
        if (!okiboMapCooldown.execute(ActionGroupContext(gearyPlayer))) return

        if (map.location.world != player.world || map.location.distance(player.location) > MAX_BOARD_DISTANCE)
            return player.error("You are not near a station!")
        if (origin == destination) return player.error("You are already at that station!")

        val railDistance = okibo.railDistance(origin, destination) ?: run {
            okibo.requestReroute(origin)
            return player.error("The okiboline is still warming up, try again in a moment!")
        }
        val cost = okibo.cost(railDistance)

        val selected = gearyPlayer.get<OkiboTraveler>()?.takeIf { it.isValid(config.confirmTimeout) }
        if (selected?.destinationId != destination.id) {
            gearyPlayer.set(OkiboTraveler(destination.id))
            player.info("<gold>Do you want a ride to <i>${destination.displayName}</i>?")
            if (cost > 0) player.info("<gold>It will cost you <i>$cost</i> orth coins")
            return player.info("<gold>Click again to confirm!")
        }

        gearyPlayer.remove<OkiboTraveler>()
        if (cost > (player.playerDataOrNull?.orthCoinsHeld ?: 0))
            return player.error("You do not have enough coins to travel to that station!")
        if (!okibo.spawnCart(player, origin, destination))
            return player.error("Your ride could not be summoned, please let staff know!")
        if (cost > 0) player.editPlayerData { orthCoinsHeld -= cost }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.toGeary().remove<OkiboTraveler>()
    }
}
