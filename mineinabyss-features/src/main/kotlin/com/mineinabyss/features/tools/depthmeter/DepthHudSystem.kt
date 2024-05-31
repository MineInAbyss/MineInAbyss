package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.geary.systems.builders.system
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.time.ticks
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareGrindstoneEvent

fun GearyModule.createDepthHudSystem() = system(query<ShowDepthMeterHud>()).every(5.ticks).execOnAll {
    /*val oldPlayersWithHud = hudEnabledQuery.entities().toSet()
    val newPlayersWithHud = mutableSetOf<GearyEntity>()
    forEach {
        val player = entity.parent ?: return@forEach
        newPlayersWithHud += player
    }

    // Update component on players that need an update
    oldPlayersWithHud.minus(newPlayersWithHud).forEach {
        it.remove<ShowDepthMeterHud>()
    }
    newPlayersWithHud.minus(oldPlayersWithHud).forEach {
        it.set(ShowDepthMeterHud())
    }*/
}

private val hudEnabledQuery = geary.cache(object : GearyQuery() {
    override fun ensure() = this {
        has<Player>()
        has<ShowDepthMeterHud>()
    }
})
