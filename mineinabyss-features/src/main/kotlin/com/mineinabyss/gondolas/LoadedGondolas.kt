package com.mineinabyss.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.autoscan.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener

object LoadedGondolas : GearyListener() {
    val TargetScope.gondola by get<Gondola>()

    init {
        allAdded()
    }

    val loaded = mutableMapOf<String, Gondola>()

    @Handler
    fun addToMap(affected: TargetScope) {
        loaded[affected.gondola.name] = affected.gondola
    }
}
