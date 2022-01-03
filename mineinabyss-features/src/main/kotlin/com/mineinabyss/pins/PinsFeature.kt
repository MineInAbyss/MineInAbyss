package com.mineinabyss.pins

import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import com.mineinabyss.pins.ui.PinMenu
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("pins")
class PinsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            PinDropListener()
        )

        geary {
            systems(
                PinActivatorSystem(),
                AbyssalPinBehaviour(),
                AddPinBehaviour(),
            )

            bukkitEntityAssociations {
                onEntityRegister<Player> {
                    //TODO kotlin bug, removing this defaults it to Unit but only sometimes
                    getOrSetPersisting<ActivePins> { ActivePins() }
                }
            }
        }

        commands {
            mineinabyss {
                "pin" {
                    permission = "mineinabyss.pin"
                    "add" {
                        val key by arg<PrefabKey> {
                            parseBy { PrefabKey.of(passed) }
                        }

                        playerAction {
                            val pins = player.toGeary().getOrSetPersisting { ActivePins() }
                            pins.add(key)
                        }
                    }
                }

                "pins" {
                    permission = "mineinabyss.pins.menu"
                    playerAction {
                        guiy { PinMenu(player) }
                    }
                }
            }
        }
    }
}
