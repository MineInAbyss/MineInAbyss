package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.systems.query.query
import kotlin.collections.set

/*
 * Object containing the list of all active gondolas
 * However, it seems that a player can have a gondola "unlocked" without it being loaded (and thus part of this map)
 * Therefore, this map should represent the list of all "active" gondolas, that is, that can be used
 * This implies that a player can unlock a gondola that can become inactive in one way or another
 * The main benefit is that it allows to toggle gondolas on and off
 * However this also means that we need to keep track of both the loaded gondolas and all the existing gondolas
 * Therefore, this object contains the list of all **active** gondolas, which is different from the list of all **existing** gondolas
 */
object LoadedGondolas {
    //private val tracker = gearyPaper.gearyModule.setup.geary.observe<OnSet>().exec(query<Gondola>()) { (gondola) ->
    //    loaded[gondola.name] = gondola
    //}

    val loaded = mutableMapOf<String, Gondola>()
}
