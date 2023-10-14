package com.mineinabyss.features.helpers.di

import com.mineinabyss.features.layers.LayersFeature
import com.mineinabyss.features.okibotravel.OkiboTravelFeature
import com.mineinabyss.idofront.di.DI

object Features {
    val layers: LayersFeature by DI.observe()
    val okiboLine: OkiboTravelFeature by DI.observe()
}
