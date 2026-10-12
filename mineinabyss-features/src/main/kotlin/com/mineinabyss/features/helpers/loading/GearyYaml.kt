package com.mineinabyss.features.helpers.loading

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.serialization.SerializableComponents

// Geary actions and conditions look up their serializers through the Geary world in this module
fun gearyYaml() = Yaml(
    serializersModule = gearyPaper.worldManager.global.getAddon(SerializableComponents).formats.module,
    configuration = YamlConfiguration(
        strictMode = false,
        polymorphismStyle = PolymorphismStyle.Property,
        polymorphismPropertyName = "type",
        anchorsAndAliases = AnchorsAndAliases.Permitted(),
    ),
)
