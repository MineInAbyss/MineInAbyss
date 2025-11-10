package com.mineinabyss.features.descent

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.features.layers.LayersFeature
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.info
import org.koin.core.module.dsl.scopedOf

val DescentFeature = feature("descent") {
    dependsOn {
        features(LayersFeature)
    }

    scopedModule {
        scopedOf(::DescentListener)
    }

    onEnable {
        listeners(get<DescentListener>())
    }

    mainCommand {
        "start" {
            executes.asPlayer {
                player.toGeary().apply {
                    if (has<DescentContext>())
                        fail("You are already ingame!\nYou can leave using /stopdescent")
                    setPersisting(DescentContext())
                }
            }
        }
        "stopdescent" {
            executes.asPlayer {
                with(player.toGeary()) {
                    val descent = get<DescentContext>()
                        ?: fail("You are not currently ingame!\nStart by using /start")
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
