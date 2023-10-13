package com.mineinabyss.features.helpers.di

import com.mineinabyss.features.layers.LayersFeature
import com.mineinabyss.idofront.di.DI

object Features {
    val layers: LayersFeature by DI.observe()
}
