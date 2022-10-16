package com.mineinabyss.plugin

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.curse.effects.*
import com.mineinabyss.idofront.autoscan.polymorphicSerializer
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.config.ReloadScope
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.mineinabyss.core.mineInAbyss
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.reflections.Reflections

class MineInAbyssConfig : IdofrontConfig<MIAConfig.Data>(
    mineInAbyss, MIAConfig.Data.serializer(),
    format = Yaml(
        // We autoscan in our Feature classes so need to use Geary's module.
        serializersModule = Reflections("com.mineinabyss", MineInAbyssConfig::class.java.classLoader)
            .getSubTypesOf(AbyssFeature::class.java)
            .map { it.kotlin }
            .polymorphicSerializer() + SerializersModule {
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
        "Enabling features" {
            data.features.forEach {
                it.apply {
                    val featureName = it::class.simpleName
                    attempt(success = "Enabled $featureName", fail = "Failed to enable $featureName") {
                        mineInAbyss.enableFeature()
                    }.onFailure(Throwable::printStackTrace)
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
