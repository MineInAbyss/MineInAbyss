package com.mineinabyss.plugin

//class MineInAbyssConfig : IdofrontConfig<MIAConfig.Data>(
//    mineInAbyss, MIAConfig.Data.serializer(),
//    format = Yaml(
//        // We autoscan in our Feature classes so need to use Geary's module.
//        serializersModule = Reflections("com.mineinabyss", MineInAbyssConfig::class.java.classLoader)
//            .getSubTypesOf(AbyssFeature::class.java)
//            .map { it.kotlin }
//            .polymorphicSerializer() + SerializersModule {
//            polymorphic(AscensionEffect::class) {
//                subclass(DeathAscensionEffect.serializer())
//                subclass(ParticleAscensionEffect.serializer())
//                subclass(PotionAscensionEffect.serializer())
//                subclass(SoundAscensionEffect.serializer())
//                subclass(MaxHealthChangeEffect.serializer())
//                subclass(HallucinatingAscensionEffect.serializer())
//            }
//        },
//        configuration = YamlConfiguration(
//            extensionDefinitionPrefix = "x-"
//        )
//    )
//), MIAConfig {
//    override fun ReloadScope.load() {
//        "Enabling features" {
//            data.features.forEach {
//                it.apply {
//                    val featureName = it::class.simpleName
//                    attempt(success = "Enabled $featureName", fail = "Failed to enable $featureName") {
//                        mineInAbyss.enableFeature()
//                    }.onFailure(Throwable::printStackTrace)
//                }
//            }
//        }
//    }
//
//    override fun ReloadScope.unload() {
//        "Disabling features" {
//            data.features.forEach {
//                it.apply {
//                    "Disabled ${it::class.simpleName}" {
//                        mineInAbyss.disableFeature()
//                    }
//                }
//            }
//        }
//    }
//}
