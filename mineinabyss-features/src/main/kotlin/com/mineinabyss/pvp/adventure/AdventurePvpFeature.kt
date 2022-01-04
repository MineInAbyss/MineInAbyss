package com.mineinabyss.pvp.adventure

import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.helpers.isInHub
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.pvp.PvpDamageListener
import com.mineinabyss.pvp.PvpPrompt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("adventure_pvp")
class AdventurePvpFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            PvpDamageListener(),
            AdventurePvpListener()
        )

        commands {
            mineinabyss {
                "pvp"(desc = "Opens PvP Selection menu") {
                    permission = "mineinabyss.pvp"
                    playerAction {
                        if (!player.isInHub()) {
                            player.error("Pvp can only be toggled in Orth")
                            return@playerAction
                        }
                        guiy { PvpPrompt(player) }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "pvp"
                    ).filter { it.startsWith(args[0]) }
                    else -> null
                }
            }
        }
    }
}
