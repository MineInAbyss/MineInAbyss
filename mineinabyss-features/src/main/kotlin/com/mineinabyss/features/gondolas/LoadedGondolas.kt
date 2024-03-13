package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import kotlin.collections.set

object LoadedGondolas {
    private val tracker = geary.listener(object : ListenerQuery() {
        val gondola by get<Gondola>()
        override fun ensure() = event.anySet(::gondola)
    }).exec {
        loaded[gondola.name] = gondola
    }

    val loaded = mutableMapOf<String, Gondola>()
}
