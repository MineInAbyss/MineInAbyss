package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import kotlin.collections.set

object LoadedGondolas {
    private val tracker = geary.observe<OnSet>().exec(query<Gondola>()) { (gondola) ->
        loaded[gondola.name] = gondola
    }

    val loaded = mutableMapOf<String, Gondola>()
}
