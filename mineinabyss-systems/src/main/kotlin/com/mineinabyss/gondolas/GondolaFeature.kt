package com.mineinabyss.gondolas

import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.serialization.toSerializable
import com.mineinabyss.mineinabyss.adventure.gondolas.SpawnLocation
import com.mineinabyss.mineinabyss.adventure.gondolas.SpawnLocationsConfig
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material

@Serializable
@SerialName("gondolas")
class GondolaFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        commands {
            ("mineinabyss" / "mia") {
                "creategondolaspawn" {
                    val displayName by stringArg { default = "" }
                    val cost by intArg { default = 0 }

                    playerAction {
                        SpawnLocationsConfig.data.spawns
                        val displayItem = player.inventory.itemInMainHand

                        if (displayItem.type == Material.AIR)
                            displayItem.type = Material.GRASS_BLOCK

                        displayItem.editItemMeta {
                            if (displayName.isNotEmpty())
                                setDisplayName(displayName)
//                    addItemFlags(ItemFlag.HIDE_ATTRIBUTES) //TODO probably better to add these tags to the serialized items
//                    addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                        }

                        SpawnLocationsConfig.data.spawns.add(
                            SpawnLocation(
                                location = player.location,
                                displayItem = displayItem.toSerializable(),
                                cost = cost
                            )
                        )
                        SpawnLocationsConfig.queueSave()

                        sender.success("Created spawn")
                    }
                }
            }
        }
    }
}
