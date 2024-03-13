package com.mineinabyss.features.relics

import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.cachedQuery
import com.mineinabyss.geary.systems.builders.system
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.Player

fun GearyModule.trackStarCompassHudOnPlayersSystem() = system(object : ListenerQuery() {
    private val hudShown by get<ShowStarCompassHud>()
}).every(5.ticks).execOnAll {
    val oldPlayersWithHud = hudEnabledQuery.entities().toSet()
    val newPlayersWithHud = mutableSetOf<GearyEntity>()

    forEach {
        val player = entity.parent ?: return@forEach
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

private val hudEnabledQuery = geary.cachedQuery(object : GearyQuery() {
    val player by get<Player>()
    val hudShown by get<ShowStarCompassHud>()
})
