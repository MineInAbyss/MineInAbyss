package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("gondolas")
class GondolaFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        geary.pipeline.addSystems(
            LoadedGondolas,
            GondolaTracker()
        )

        commands {
            mineinabyss {
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
}
