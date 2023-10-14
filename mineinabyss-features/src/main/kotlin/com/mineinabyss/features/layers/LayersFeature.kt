package com.mineinabyss.features.layers

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.idofront.config.ConfigFormats
import com.mineinabyss.idofront.config.Format
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.Configurable
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("layers")
class LayersFeature : AbyssFeature, Configurable<LayersConfig> {
    @Transient
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

    @Transient
    val worldManager: AbyssWorldManager = SingleAbyssWorldManager(config.layers)

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(LayerListener())
    }
}
