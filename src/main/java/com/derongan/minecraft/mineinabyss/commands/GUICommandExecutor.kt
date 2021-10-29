package com.derongan.minecraft.mineinabyss.commands

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.mineinabyss.configuration.MIAConfig
import com.derongan.minecraft.mineinabyss.configuration.SpawnLocation
import com.derongan.minecraft.mineinabyss.configuration.SpawnLocationsConfig
import com.derongan.minecraft.mineinabyss.ecs.components.DescentContext
import com.derongan.minecraft.mineinabyss.ecs.components.pins.ActivePins
import com.derongan.minecraft.mineinabyss.ecs.systems.OrthReturnSystem
import com.derongan.minecraft.mineinabyss.gui.pins.PinMenu
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.player.openHubStorage
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.serialization.toSerializable
import org.bukkit.Material

@ExperimentalCommandDSL
object GUICommandExecutor : IdofrontCommandExecutor() {
    override val commands = commands(mineInAbyss) {
        ("mineinabyss" / "mia") command@{
            "pin" {
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
                playerAction {
                    guiy { PinMenu(player) }
                }
            }

            "start" {
                playerAction {
                    player.toGeary().apply {
                        if (has<DescentContext>())
                            this@command.stopCommand("You are already ingame!\nYou can leave using /stopdescent")
                        setPersisting(DescentContext())
                    }
//                    GondolaGUI(player).show(player)
                }
            }
            "stats" {
                "subcommand" {

                }
                playerAction {
                    // StatsGUI(player).show(player)
                }
            }
            "storage" {
                playerAction {
                    if (MIAConfig.data.hubSection == WorldManager.getSectionFor(player.location))
                        player.openHubStorage()
                    else
                        sender.error("You are not in the hub area")
                }
            }
            "stopdescent" {
                playerAction {
                    with(player.toGeary()) {
                        val descent = get<DescentContext>()
                            ?: this@command.stopCommand("You are not currently ingame!\nStart by using /start")
                        if (!descent.confirmedLeave) {
                            descent.confirmedLeave = true
                            sender.info(
                                """
                        &cYou are about to leave the game!!!
                        &lYour progress will be lost&r&c, but any xp and money you earned will stay with you.
                        Type /stopdescent again to leave
                        """.trimIndent().color()
                            )
                        } else {
                            player.health = 0.0
                            OrthReturnSystem.removeDescentContext(player)
                        }
                    }
                }
            }
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
