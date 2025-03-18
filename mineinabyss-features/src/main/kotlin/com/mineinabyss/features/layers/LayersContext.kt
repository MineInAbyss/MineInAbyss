package com.mineinabyss.features.layers

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.serialization.SerializableComponents
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
                        serializersModule = abyss.gearyGlobal.getAddon(SerializableComponents).serializers.module,
                        configuration = YamlConfiguration(
                            extensionDefinitionPrefix = "x-",
                            anchorsAndAliases = AnchorsAndAliases.Permitted(),
                        )
                    )
                )
            )
        )
    )

    val worldManager: AbyssWorldManager = SingleAbyssWorldManager(config.layers)
    val layersListener = LayerListener()
}
