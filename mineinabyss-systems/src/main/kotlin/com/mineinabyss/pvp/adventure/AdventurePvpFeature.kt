package com.mineinabyss.pvp.survival

import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.isInHub
import com.mineinabyss.pvp.PvpDamageListener
import com.mineinabyss.pvp.adventure.AdventurePvpListener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("adventure_pvp")
@ExperimentalCommandDSL
class AdventurePvpFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            PvpDamageListener(),
            AdventurePvpListener()
        )

        commands {
            ("mineinabyss" / "mia") {
                "pvp"(desc = "Opens PvP Selection menu") {
                    action {
                        val player = sender as? Player ?: return@action
                        if (!player.isInHub()) {
                            player.error("Pvp can only be toggled in Orth")
                            return@action
                        }

                        guiy {
                            Chest(listOf(player), title = "Insert Unicode Here", 4, onClose = { reopen() }) {
                                Grid(3, 2, Modifier.at(1,1).clickable
                                {
                                    player.playerData.pvpStatus = true
                                    player.playerData.pvpUndecided = false
                                    player.success("PvP has been enabled!")
                                    player.closeInventory()
                                })
                                {
                                    repeat(6){
                                        Item(ItemStack(Material.PAPER).editItemMeta {
                                            setCustomModelData(1)
                                            setDisplayName("${ChatColor.GREEN}${ChatColor.BOLD}Enable PvP")
                                        })
                                    }
                                }
                                Grid(3, 2, Modifier.at(5,1).clickable {
                                    player.playerData.pvpStatus = false
                                    player.playerData.pvpUndecided = false
                                    player.error("PvP has been disabled!")
                                    player.closeInventory()
                                }) {
                                    repeat(6){
                                        Item(ItemStack(Material.PAPER).editItemMeta {
                                            setCustomModelData(1)
                                            setDisplayName("${ChatColor.RED}${ChatColor.BOLD}Disable PvP")
                                        })
                                    }
                                }
                                Grid(1,1,Modifier.at(8,3).clickable {
                                    val data = player.playerData
                                    data.showPvPMessage = !data.showPvPMessage
                                    player.success("PvP-prompt has been ${if (data.showPvPMessage) "${ChatColor.BOLD}enabled" else "${ChatColor.BOLD}disabled"}.")
                                    player.closeInventory()
                                }) { Item(ItemStack(Material.PAPER).editItemMeta {
                                    setCustomModelData(1)
                                    setDisplayName("${ChatColor.BLUE}${ChatColor.BOLD}Toggle PvP Prompt")
                                }) }
                            }
                        }
                    }
                }
            }
        }
    }
}