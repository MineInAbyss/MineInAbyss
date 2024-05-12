package com.mineinabyss.features.descent

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.plugin.listeners

class DescentFeature : Feature() {
    override fun FeatureDSL.enable() {
        plugin.listeners(
            DescentListener()
        )

        mainCommand {
            "start" {
                playerAction {
                    player.toGeary().apply {
                        if (has<DescentContext>())
                            this@mainCommand.stopCommand("You are already ingame!\nYou can leave using /stopdescent")
                        setPersisting(DescentContext())
                    }
//                    GondolaGUI(player).show(player)
                }
            }
            "stopdescent" {
                playerAction {
                    with(player.toGeary()) {
                        val descent = get<DescentContext>()
                            ?: this@mainCommand.stopCommand("You are not currently ingame!\nStart by using /start")
                        if (!descent.confirmedLeave) {
                            descent.confirmedLeave = true
                            sender.info(
                                """
                        <red>You are about to leave the game!!!
                        <b>Your progress will be lost</b>, but any xp and money you earned will stay with you.
                        Type /stopdescent again to leave.
                        """.trimIndent()
                            )
                        } else {
                            player.health = 0.0
                            player.removeDescentContext()
                        }
                    }
                }
            }
        }
    }
}
