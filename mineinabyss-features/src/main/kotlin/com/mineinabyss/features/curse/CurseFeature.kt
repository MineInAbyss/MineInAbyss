package com.mineinabyss.features.curse

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.idofront.commands.arguments.booleanArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners

class CurseFeature : Feature() {
    override val dependsOn = setOf("DeeperWorld")

    override fun FeatureDSL.enable() {
        plugin.listeners(CurseAscensionListener(), CurseEffectsListener())

        mainCommand {
            "curse"(desc = "Commands to toggle curse") {
                permission = "mineinabyss.curse"

                val toggled by booleanArg()

                playerAction {
                    player.editPlayerData { isAffectedByCurse = toggled }
                    val enabled = if (toggled) "enabled" else "disabled"
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
