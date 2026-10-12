package com.mineinabyss.features.warps

import com.mineinabyss.features.cinematics.isInCinematic
import com.mineinabyss.features.dialogs.dialogSessions
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*

// EntityInsideBlockEvent never fires for tripwire while Paper's disable-tripwire-updates is on, which Nexo string blocks need
class WarpTriggerListener(
    private val travel: WarpTravel,
    private val warps: Warps,
    private val triggerBlocks: WarpTriggerBlocks,
) : Listener {
    // Stop a player is standing in, its triggers stay quiet until they step off onto a non-trigger block
    private val boarded = mutableMapOf<UUID, TriggerStop>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerMoveEvent.onMove() {
        if (!hasChangedBlock()) return
        val stop = triggerBlocks.stopAt(to.block) ?: return run { boarded.remove(player.uniqueId) }
        if (boarded.put(player.uniqueId, stop) == stop) return
        if (warps.isWarping(player) || player.isInCinematic() || dialogSessions?.isInDialog(player) == true) return
        travel.offer(player, stop.warp, from = stop.stop)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerTeleportEvent.onTeleport() {
        triggerBlocks.stopAt(to.block)?.let { boarded[player.uniqueId] = it } ?: boarded.remove(player.uniqueId)
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        boarded.remove(player.uniqueId)
    }
}
