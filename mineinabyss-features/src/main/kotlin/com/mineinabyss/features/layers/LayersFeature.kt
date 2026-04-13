package com.mineinabyss.features.layers

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.gets
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.requirePlugins
import com.mineinabyss.idofront.features.singleConfig

val LayersFeature = module("layers") {
    require(get<AbyssFeatureConfig>().layers.enabled) { "Layers feature is disabled" }
    requirePlugins("DeeperWorld")

    val config by singleConfig<LayersConfig>("layers.yml") {
        format = Yaml(
            serializersModule = abyss.gearyGlobal.getAddon(SerializableComponents).formats.module,
            configuration = YamlConfiguration(
                extensionDefinitionPrefix = "x-",
                anchorsAndAliases = AnchorsAndAliases.Permitted(),
            )
        )
    }
    single<AbyssWorldManager> { SingleAbyssWorldManager(config.layers) }
    listeners(LayerListener())

    single<LayersContext> {
        object : LayersContext {
            override val worldManager = get<AbyssWorldManager>()
            override val layersListener = get<LayerListener>()
        }
    }
}.gets<LayersContext>()