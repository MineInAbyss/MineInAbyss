package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import kotlin.collections.set

object LoadedGondolas : GearyListener() {
    val Pointers.gondola by get<Gondola>().whenSetOnTarget()

    val loaded = mutableMapOf<String, Gondola>()

    override fun Pointers.handle() {
        loaded[gondola.name] = gondola
    }
}
