package com.mineinabyss.gondolas

import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("gondolas")
class GondolaFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        commands {
            ("mineinabyss" / "mia") {
                "gondola"(desc = "Opens the gondola menu") {
                    playerAction {
                        guiy { GondolaSelectionMenu(player) }
                    }
                }
            }
        }
    }
}
