package com.mineinabyss.features.keepinventory

import com.mineinabyss.components.playerData
import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.commands.arguments.booleanArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.unregisterListeners
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

class KeepInvFeature(config: Config) : FeatureWithContext<KeepInvFeature.Context>({ Context(config) }) {
    @Serializable
    class Config {
        val enabled = true
        val keepInvInVoid: Boolean = true
    }

    class Context(val config: Config) {
        val listeners = buildList {
            add(KeepInvListener(config))
            if (abyss.isEternalFortuneLoaded) KeepInvGraveListener(config)
        }.toTypedArray()
    }

    override fun FeatureDSL.enable() {
        plugin.listeners(*context.listeners)

        mainCommand {
            "keepinv"(desc = "Commands to toggle keepinventory status") {
                val toggled by booleanArg()

                playerAction {
                    val player = sender as Player
                    player.playerData.keepInvStatus = toggled
                    if (toggled) {
                        player.playerData.keepInvStatus = true
                        player.success("Keep Inventory enabled for ${player.name}")
                    } else {
                        player.playerData.keepInvStatus = false
                        sender.error("Keep Inventory disabled for ${player.name}")
                    }
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf(
                    "keepinv"
                ).filter { it.startsWith(args[0]) }

                2 -> {
                    when (args[0]) {
                        "keepinv" -> listOf("on", "off")
                        else -> null
                    }
                }

                else -> null
            }
        }
    }

    override fun FeatureDSL.disable() {
        plugin.unregisterListeners(*context.listeners)
    }
}
