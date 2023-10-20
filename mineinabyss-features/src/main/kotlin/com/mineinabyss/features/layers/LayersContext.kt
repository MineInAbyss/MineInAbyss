package com.mineinabyss.features.layers

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.idofront.config.ConfigFormats
import com.mineinabyss.idofront.config.Format
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable

class LayersContext: Configurable<LayersConfig> {
    override val configManager = config(
        "layers", abyss.plugin.dataFolder.toPath(), LayersConfig(), formats = ConfigFormats(
            overrides = listOf(
                Format(
                    "yml", Yaml(
                        // We autoscan in our Feature classes so need to use Geary's module.
                        serializersModule = serializableComponents.serializers.module,
                        configuration = YamlConfiguration(
                            extensionDefinitionPrefix = "x-",
                            allowAnchorsAndAliases = true,
                        )
                    )
                )
            )
        )
    )

    val worldManager: AbyssWorldManager = SingleAbyssWorldManager(config.layers)
    val layersListener = LayerListener()
}
