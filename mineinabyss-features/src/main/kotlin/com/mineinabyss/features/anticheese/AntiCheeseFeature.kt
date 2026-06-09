package com.mineinabyss.features.anticheese

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.features.layers.LayersConfig
import com.mineinabyss.features.layers.LayersFeature
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.singleConfig

val AntiCheeseFeature = module("anticheese") {
    require(get<AbyssFeatureConfig>().antiCheese.enabled) { "AntiCheese feature is disabled" }
    val config by singleConfig<AntiCheeseConfig>("anticheese.yml") {
        format = Yaml(
            serializersModule = abyss.gearyGlobal.getAddon(SerializableComponents).formats.module,
            configuration = YamlConfiguration(
                extensionDefinitionPrefix = "x-",
                anchorsAndAliases = AnchorsAndAliases.Permitted(),
            )
        )
    }
    singleModule(LayersFeature)
    listeners(AntiCheeseListener(config))
}
