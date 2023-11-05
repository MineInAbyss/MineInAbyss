package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success

class GondolaFeature : Feature {
    override fun FeatureDSL.enable() {
        geary.pipeline.addSystems(
            LoadedGondolas,
            GondolaTracker()
        )

        mainCommand {
            "gondola"(desc = "Commands for gondolas") {
                permission = "mineinabyss.gondola"
                "list"(desc = "Opens the gondola menu") {
                    permission = "mineinabyss.gondola.list"
                    playerAction {
                        guiy { GondolaSelectionMenu(player) }
                    }
                }
                "unlock"(desc = "Unlocks a gondola for a player") {
                    permission = "mineinabyss.gondola.unlock"
                    val gondola by stringArg()
                    playerAction {
                        val gondolas = player.toGeary().get<UnlockedGondolas>() ?: return@playerAction
                        gondolas.keys.add(gondola)
                        player.success("Unlocked $gondola")
                    }
                }
                "clear"(desc = "Removes all associated gondolas from a player") {
                    permission = "mineinabyss.gondola.clear"
                    playerAction {
                        val gondolas = player.toGeary().getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
                        gondolas.keys.clear()
                        player.error("Cleared all gondolas")
                    }
                }
            }
        }
    }
}
