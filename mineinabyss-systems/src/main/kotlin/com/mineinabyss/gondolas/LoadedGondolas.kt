package com.mineinabyss.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.ComponentAddSystem

object LoadedGondolas : ComponentAddSystem() {
    val loaded = mutableMapOf<String, Gondola>()

    private val GearyEntity.gondola by get<Gondola>()

    override fun GearyEntity.run() {
        loaded[gondola.name] = gondola
    }
}
