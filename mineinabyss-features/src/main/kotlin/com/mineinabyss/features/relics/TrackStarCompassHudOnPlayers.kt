package com.mineinabyss.features.relics

import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.Player

class TrackStarCompassHudOnPlayers : RepeatingSystem(5.ticks) {
    private val Pointer.hudShown by get<ShowStarCompassHud>()

    private val hudEnabledQuery = object: GearyQuery() {
        val Pointer.player by get<Player>()
        val Pointer.hudShown by get<ShowStarCompassHud>()
    }

    @OptIn(UnsafeAccessors::class)
    override fun tickAll() {
        val oldPlayersWithHud = hudEnabledQuery.matchedEntities.toSet()
        val newPlayersWithHud = mutableSetOf<GearyEntity>()
        forEach {
            val player = it.entity.parent ?: return@forEach
            newPlayersWithHud += player
        }

        // Update component on players that need an update
        oldPlayersWithHud.minus(newPlayersWithHud).forEach {
            it.remove<ShowStarCompassHud>()
        }
        newPlayersWithHud.minus(oldPlayersWithHud).forEach {
            it.set(ShowStarCompassHud())
        }
    }
}
