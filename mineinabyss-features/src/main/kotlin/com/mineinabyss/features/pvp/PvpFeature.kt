package com.mineinabyss.features.pvp

import com.mineinabyss.features.helpers.layer
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.entity.Player

class PvpFeature : Feature() {
    override fun FeatureDSL.enable() {
        plugin.listeners(
            PvpDamageListener(),
            PvpListener()
        )

        mainCommand {
            "pvp"(desc = "Commands to toggle pvp status") {
                playerAction {
                    val player = sender as Player
                    if (player.location.layer?.hasPvpDefault == true) {
                        player.error("Pvp cannot be toggled in this layer.")
                        return@playerAction
                    }
                    guiy { PvpPrompt(player) }
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("pvp").filter { it.startsWith(args.first()) }
                else -> emptyList()
            }
        }
    }
}
