package com.mineinabyss.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import kotlin.collections.mutableMapOf
import kotlin.collections.set

object LoadedGondolas : GearyListener() {
    val TargetScope.gondola by added<Gondola>()

    val loaded = mutableMapOf<String, Gondola>()

    @Handler
    fun addToMap(affected: TargetScope) {
        loaded[affected.gondola.name] = affected.gondola
    }
}
