package com.mineinabyss.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import kotlin.collections.set

object LoadedGondolas : GearyListener() {
    val TargetScope.gondola by onSet<Gondola>()

    val loaded = mutableMapOf<String, Gondola>()

    @Handler
    fun addToMap(affected: TargetScope) {
        loaded[affected.gondola.name] = affected.gondola
    }
}
