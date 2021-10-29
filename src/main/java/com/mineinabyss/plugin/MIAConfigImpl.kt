package com.mineinabyss.plugin

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.components.AscensionEffect
import com.mineinabyss.curse.effects.*
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.config.ReloadScope
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.mineinabyss.core.mineInAbyss
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class MIAConfigImpl : IdofrontConfig<MIAConfig.Data>(
    mineInAbyss, MIAConfig.Data.serializer(),
    format = Yaml(
        // We autoscan in our Feature classes so need to use Geary's module.
        serializersModule = Formats.module + SerializersModule {
            polymorphic(AscensionEffect::class) {
                subclass(DeathAscensionEffect.serializer())
                subclass(ParticleAscensionEffect.serializer())
                subclass(PotionAscensionEffect.serializer())
                subclass(SoundAscensionEffect.serializer())
                subclass(MaxHealthChangeEffect.serializer())
                subclass(HallucinatingAscensionEffect.serializer())
            }
        },
        configuration = YamlConfiguration(
            extensionDefinitionPrefix = "x-"
        )
    )
), MIAConfig {
    override fun ReloadScope.load() {
        "Disabling features" {
            data.features.forEach {
                it.apply {
                    "Enabled ${it::class.simpleName}" {
                        mineInAbyss.enableFeature()
                    }
                }
            }
        }
    }

    override fun ReloadScope.unload() {
        "Disabling features" {
            data.features.forEach {
                it.apply {
                    "Disabled ${it::class.simpleName}" {
                        mineInAbyss.disableFeature()
                    }
                }
            }
        }
    }
}
