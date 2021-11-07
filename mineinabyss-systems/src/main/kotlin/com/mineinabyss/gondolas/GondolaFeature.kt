package com.mineinabyss.gondolas

import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("gondolas")
@AutoscanComponent
class GondolaFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {

        geary {
            systems(LoadedGondolas)
            bukkitEntityAssociations {
                onEntityRegister<Player> {
                    //TODO kotlin bug, removing this defaults it to Unit but only sometimes
                    getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
                }
            }
        }

        commands {
            ("mineinabyss" / "mia") {
                "gondola"(desc = "Commands for gondolas") {
                    permission = "mineinabyss.gondola"
                    "list"(desc = "Opens the gondola menu") {
                        permission = "mineinabyss.gondola.list"
                        playerAction {
                            guiy { GondolaSelectionMenu(player) }
                        }
                    }
                    "create" {
                        permission = "mineinabyss.gondola.create"
                        playerAction {
                            val gondolas = player.toGeary().getOrSetPersisting { UnlockedGondolas() }.broadcastVal("Gondolas: ")
                        }
                    }
                    "unlock"(desc = "Unlocks a gondola for a player") {
                        permission = "mineinabyss.gondola.unlock"
                        val gondola by stringArg()
                        playerAction {
                            val gondolas = player.toGeary().getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
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
