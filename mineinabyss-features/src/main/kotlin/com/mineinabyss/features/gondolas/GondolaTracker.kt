package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import org.bukkit.entity.Player

fun GearyModule.createGondolaTracker() = listener(object : ListenerQuery() {
    val player by get<Player>()
    override fun ensure() = event.anySet(::player)
}).exec {
    event.entity.getOrSetPersisting { UnlockedGondolas() }
}
