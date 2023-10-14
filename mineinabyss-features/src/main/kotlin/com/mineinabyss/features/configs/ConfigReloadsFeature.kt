package com.mineinabyss.features.configs

import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyssFeatures
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("config_reloads")
class ConfigReloadsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        commands {
            mineinabyss {
                "reloadFeature" {
                    val featureName by optionArg(abyssFeatures.features.map { it::class.simpleName!! })
                    action {
                        abyssFeatures.reloadFeature(featureName, sender)
                    }
                }
                "reload" {
                    // TODO full plugin reload
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("reload", "reloadFeature").filter { it.startsWith(args[0], ignoreCase = true) }

                    2 -> when (args[0]) {
                        "reloadFeature" -> abyssFeatures.features.map { it::class.simpleName!! }
                            .filter { it.startsWith(args[1], ignoreCase = true) }

                        else -> null
                    }

                    else -> null
                }
            }
        }
    }
}
