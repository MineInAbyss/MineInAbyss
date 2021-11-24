package com.mineinabyss.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyHandlerScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.onComponentAdd

object LoadedGondolas : GearyListener() {
    private val ResultScope.gondola by get<Gondola>()

    val loaded = mutableMapOf<String, Gondola>()

    override fun GearyHandlerScope.register() {

        onComponentAdd {
            loaded[gondola.name] = gondola

        }
    }
}
