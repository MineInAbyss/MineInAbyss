package com.mineinabyss.pvp.adventure

import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.isInHub
import com.mineinabyss.pvp.PvpDamageListener
import com.mineinabyss.pvp.PvpPrompt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("adventure_pvp")
class AdventurePvpFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            PvpDamageListener(),
            AdventurePvpListener()
        )

        commands {
            ("mineinabyss" / "mia") {
                "pvp"(desc = "Opens PvP Selection menu") {
                    permission = "mineinabyss.pvp"
                    action {
                        val player = sender as? Player ?: return@action
                        if (!player.isInHub()) {
                            player.error("Pvp can only be toggled in Orth")
                            return@action
                        }
                        guiy { PvpPrompt(player) }
                    }
                }
            }
        }
    }
}