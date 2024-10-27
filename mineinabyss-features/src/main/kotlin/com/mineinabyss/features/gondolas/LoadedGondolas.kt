package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.systems.query.query
import kotlin.collections.set

object LoadedGondolas {
    //private val tracker = gearyPaper.gearyModule.setup.geary.observe<OnSet>().exec(query<Gondola>()) { (gondola) ->
    //    loaded[gondola.name] = gondola
    //}

    val loaded = mutableMapOf<String, Gondola>()
}
