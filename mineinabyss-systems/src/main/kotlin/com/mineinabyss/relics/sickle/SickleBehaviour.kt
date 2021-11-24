package com.mineinabyss.relics.sickle

import com.mineinabyss.components.relics.Sickle
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyHandlerScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener

class SickleBehaviour: GearyListener() {
    val ResultScope.sickle by get<Sickle>()

    override fun GearyHandlerScope.register() {
        //TODO
    }
}
