package com.mineinabyss.curse

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("curse")
class CurseFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            CurseAscensionListener(),
            CurseEffectsListener()
        )

        commands {
            "curseon" {
                playerAction {
                    player.playerData.isAffectedByCurse = true
                    sender.success("Curse enabled for ${player.name}")
                }
            }

            "curseoff" {
                playerAction {
                    player.playerData.isAffectedByCurse = false
                    sender.error("Curse disabled for ${player.name}")
                }
            }
        }
    }
}
