package com.mineinabyss.features.helpers.di

import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.GuildFeature
import com.mineinabyss.features.layers.LayersContext
import com.mineinabyss.features.layers.LayersFeature

object Features {
    val layers: LayersContext get() = abyss.di.scope[LayersFeature]
    val guilds get() = abyss.di.scope[GuildFeature]
}
