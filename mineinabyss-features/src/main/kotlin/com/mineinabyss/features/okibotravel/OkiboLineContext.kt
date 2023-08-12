package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.idofront.di.DI

val okiboLine by DI.observe<OkiboLineContext>()
interface OkiboLineContext {
    val okiboStations: Set<OkiboLineStation>
}
