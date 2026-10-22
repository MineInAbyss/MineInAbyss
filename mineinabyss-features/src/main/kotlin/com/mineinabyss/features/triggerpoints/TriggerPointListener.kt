package com.mineinabyss.features.triggerpoints

import com.mineinabyss.features.helpers.actionContext
import com.mineinabyss.features.helpers.isInCinematicOrTransition
import com.mineinabyss.features.helpers.isInDialog
import com.mineinabyss.features.helpers.runIn
import com.mineinabyss.geary.prefabs.PrefabKey
import io.papermc.paper.event.entity.EntityInsideBlockEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*

class TriggerPointListener(
    private val registry: TriggerPointRegistry,
    private val triggerBlocks: TriggerBlocks?,
) : Listener {
    // The tick each block trigger point a player is inside of was last seen, a point fires again only once they have left it for a tick
    private val inside = mutableMapOf<UUID, MutableMap<PrefabKey, Int>>()

    // Players who teleported onto a trigger block, it stays quiet for them until they step off
    private val arrived = mutableMapOf<UUID, PrefabKey>()

    // Nexo cancels this event for every tripwire before we see it.
    // With Paper's disable-tripwire-updates on, Cartridge only fires it for the states in block-updates.tripwire-inside-event-states
    @EventHandler(priority = EventPriority.MONITOR)
    fun EntityInsideBlockEvent.onInsideBlock() {
        val player = entity as? Player ?: return
        val point = triggerBlocks?.pointAt(block) ?: return
        val tick = Bukkit.getCurrentTick()
        val seen = inside.getOrPut(player.uniqueId) { mutableMapOf() }
        seen.values.removeIf { it < tick - 1 }
        val wasInside = seen.put(point, tick) != null
        if (arrived[player.uniqueId] == point) {
            arrived.remove(player.uniqueId)
            return
        }
        if (wasInside) return
        fire(player, point)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerMoveEvent.onMove() {
        if (!hasChangedBlock()) return
        arrived[player.uniqueId]?.let { point -> if (triggerBlocks?.pointAt(to.block) != point) arrived.remove(player.uniqueId) }

        val boxes = registry.boxes[to.world.uid] ?: return
        boxes.filter { to in it && from !in it }.forEach { fire(player, it.key) }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerTeleportEvent.onTeleport() {
        triggerBlocks?.pointAt(to.block)?.let { arrived[player.uniqueId] = it } ?: arrived.remove(player.uniqueId)
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        inside.remove(player.uniqueId)
        arrived.remove(player.uniqueId)
    }

    private fun fire(player: Player, key: PrefabKey) {
        if (player.isInCinematicOrTransition() || player.isInDialog()) return
        val point = registry[key] ?: return
        point.actions.runIn(actionContext(player, mapOf(TRIGGER_POINT_KEY to key)))
    }
}
