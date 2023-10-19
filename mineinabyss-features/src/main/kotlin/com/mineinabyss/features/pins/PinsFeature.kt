package com.mineinabyss.features.pins

import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.features.pins.ui.PinMenu
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

class PinsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        listeners(
            PinDropListener()
        )

        geary.pipeline.addSystems(
            PinActivatorSystem(),
            AbyssalPinBehaviour(),
            AddPinBehaviour(),
            PinRegistrySystem()
        )

        commands {
            mineinabyss {
                "pin" {
                    permission = "mineinabyss.pin"
                    "add" {
                        val key by arg<PrefabKey> {
                            parseBy { PrefabKey.of(passed) }
                        }

                        playerAction {
                            val player = sender as Player
                            val pins = player.toGeary().getOrSetPersisting { ActivePins() }
                            pins.add(key)
                        }
                    }
                }

                "pins" {
                    permission = "mineinabyss.pins.menu"
                    playerAction {
                        val player = sender as Player
                        guiy { PinMenu(player) }
                    }
                }
            }
        }
    }
}
