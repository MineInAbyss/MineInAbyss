package com.mineinabyss.pvp.survival

import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.pvp.DisablePvp
import com.mineinabyss.pvp.EnablePvp
import com.mineinabyss.pvp.PvpDamageListener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.ChatColor

@Serializable
@SerialName("survival_pvp")
class SurvivalPvpFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            PvpDamageListener(),
            SurvivalPvpListener()
        )

        commands {
            mineinabyss {
                "pvp"(desc = "Commands to toggle pvp status") {
                    permission = "mineinabyss.pvp"
                    playerAction {
                        if (player.location.layer?.hasPvpDefault == true) {
                            player.error("Pvp cannot be toggled in this layer.")
                            return@playerAction
                        }
                        guiy {
                            Chest(setOf(player), "${Space.of(-18)}${ChatColor.WHITE}:pvp_menu:", Modifier.height(4),
                                onClose = { reopen() }) {
                                EnablePvp(player, Modifier.at(5, 1))
                                DisablePvp(player, Modifier.at(5, 1))
                            }
                        }
                    }
                }
            }
        }
    }
}
