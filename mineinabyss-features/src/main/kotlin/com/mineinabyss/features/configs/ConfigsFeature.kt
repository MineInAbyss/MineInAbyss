package com.mineinabyss.features.configs

import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.Configurable
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("configs")
class ConfigsFeature : AbyssFeature {
    @Transient
    private val configs = abyss.config.features.filterIsInstance<Configurable<*>>().associateBy { it.configManager.name }

    override fun MineInAbyssPlugin.enableFeature() {
        commands {
            mineinabyss {
                "reloadConfig" {
                    val config by stringArg()
                    action {
                        val configurable =
                            configs[config] ?: return@action sender.error("Config not found: $config")
                        configurable.configManager.reload()
                        sender.success("Reloaded config: $config")
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("reloadConfig").filter { it.startsWith(args[0]) }

                    2 -> when (args[0]) {
                        "reloadConfig" -> configs.keys.filter { it.startsWith(args[1]) }
                        else -> null
                    }

                    else -> null
                }
            }
        }
    }
}
