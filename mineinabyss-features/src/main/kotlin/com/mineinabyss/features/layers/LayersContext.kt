package com.mineinabyss.features.layers

interface LayersContext {
    val worldManager: AbyssWorldManager
    val layersListener: LayerListener
    val config: LayersConfig
}
