package com.mineinabyss.features.commands

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.commands.brigadier.commands
import com.mineinabyss.idofront.features.onServerStartup
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.plugin.Plugin

val CommandsFeature = module("commands") {
    require(get<AbyssFeatureConfig>().commands.enabled) { "Commands feature is disabled" }
    val config by singleConfig<CommandsConfig>("commands.yml")

    onServerStartup {
        get<Plugin>().commands {
            config.commands.forEach { command ->
                command.name(aliases = command.aliases, description = command.description) {
                    permission = command.permission
                    executes {
                        command.messages.forEach { message ->
                            sender.sendMessage(message.miniMsg())
                        }
                    }
                }
            }
        }
    }
}
