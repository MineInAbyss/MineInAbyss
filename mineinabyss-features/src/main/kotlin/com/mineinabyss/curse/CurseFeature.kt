package com.mineinabyss.curse

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.arguments.booleanArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("curse")
class CurseFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            CurseAscensionListener(),
            CurseEffectsListener()
        )

        commands {
            mineinabyss {
                "curse"(desc = "Commands to toggle curse") {
                    permission = "mineinabyss.curse"

                    val toggled by booleanArg()

                    playerAction {
                        val player = sender as Player
                        player.playerData.isAffectedByCurse = toggled
                        val enabled = if(toggled) "enabled" else "disabled"
                        sender.success("Curse $enabled for ${player.name}")
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "curse"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "curse" -> listOf("on", "off")
                            else -> null
                        }
                    }
                    else -> null
                }
            }
        }

        // Curse def

    }
}
