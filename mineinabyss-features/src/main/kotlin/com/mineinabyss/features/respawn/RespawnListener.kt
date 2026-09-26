package com.mineinabyss.features.respawn

import com.mineinabyss.features.tools.depthmeter.getAbyssDepth
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent

class RespawnListener(private val config: RespawnConfig) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerMoveEvent.onMove() {
        if (from.blockY == to.blockY && from.world == to.world) return
        player.checkCrossing(from, to)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerTeleportEvent.onTeleport() {
        player.checkCrossing(from, to)
    }

    // Bonfire teleports players with a valid bonfire away from here afterward, so this also covers destroyed bonfires
    @EventHandler
    fun PlayerRespawnEvent.onRespawn() {
        if (respawnReason != PlayerRespawnEvent.RespawnReason.DEATH || isBedSpawn || isAnchorSpawn) return
        val checkpoint = player.toGeary().get<RespawnCheckpoint>() ?: return
        respawnLocation = config.byId[checkpoint.id]?.location ?: return
    }

    private fun Player.checkCrossing(from: Location, to: Location) {
        val (fromDepth, toDepth) = (from.getAbyssDepth() ?: return) to (to.getAbyssDepth() ?: return)
        if (fromDepth == toDepth) return

        val crossed = config.respawns.filter { (fromDepth < it.depth) != (toDepth < it.depth) }
        val entry = (if (toDepth > fromDepth) crossed.maxByOrNull { it.depth } else crossed.minByOrNull { it.depth }) ?: return

        val gearyPlayer = toGeary()
        if (gearyPlayer.get<RespawnCheckpoint>()?.id == entry.id) return
        gearyPlayer.setPersisting(RespawnCheckpoint(entry.id))
    }
}
