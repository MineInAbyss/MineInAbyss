package com.mineinabyss.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.ecs.accessors.EventResultScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.handlers.ComponentAddHandler

object LoadedGondolas : GearyListener() {
    private val ResultScope.gondola by get<Gondola>()

    val loaded = mutableMapOf<String, Gondola>()

    private object TrackGondolas : ComponentAddHandler() {
        override fun ResultScope.handle(event: EventResultScope) {
            loaded[gondola.name] = gondola
        }
    }
}
