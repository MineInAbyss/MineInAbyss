package com.mineinabyss.features.pvp.adventure

import com.mineinabyss.features.hubstorage.isInHub
import com.mineinabyss.features.pvp.PvpDamageListener
import com.mineinabyss.features.pvp.PvpPrompt
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.listeners

class AdventurePvpFeature : Feature() {
    override fun FeatureDSL.enable() {
        plugin.listeners(
            PvpDamageListener(),
            AdventurePvpListener()
        )

        mainCommand {
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
