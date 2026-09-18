package com.mineinabyss.features.respawn

import com.mineinabyss.features.tools.depthmeter.getAbyssDepth
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.collections.filter
import kotlin.math.abs


/* Everytime we cross a new respawn area, we set it as our new "default" respawn point */
class RespawnListener(private val config :RespawnConfig): Listener {
    @EventHandler
    fun PlayerMoveEvent.onMove() {
        if (!hasExplicitlyChangedBlock() || player.hasBonfireRespawn())
            return
        val depth = to.getAbyssDepth() ?: return
        val where = config.respawns
            .asSequence()
            .filter { it.depth <= depth }
            .maxByOrNull { it.depth } ?: return // we get the deepest spawn we're allowed to respawn at

        // we need to check that were within range of a certain spawnpoint depth
        // note: it only works downwards (since were filtering for max depth earlier on)
        val verticalTolerance = 10
        if (abs(depth - where.depth) > verticalTolerance)
            return

        // we are within range of a new respawn location, we can update it
        player.respawnLocation = where.location
        player.toGeary().setPersisting(RespawnData(where.depth.toString()))
    }
}

