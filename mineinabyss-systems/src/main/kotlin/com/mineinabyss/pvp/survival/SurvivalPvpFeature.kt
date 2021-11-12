package com.mineinabyss.pvp.survival

import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.pvp.PvpDamageListener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
                    action {
                        val player = sender as? Player ?: return@action
                        if (player.location.layer?.hasPvpDefault == true) {
                            player.error("Pvp cannot be toggled in this layer.")
                            return@action
                        }
                        guiy {
                            Chest(listOf(player), NegativeSpace.of(18) + "${ChatColor.WHITE}:pvp_menu:",
                                4, onClose = { reopen() }) {
                                Grid(3, 2, Modifier.at(1, 1).clickable
                                {
                                    player.playerData.pvpStatus = true
                                    player.success("PvP has been enabled!")
                                    player.closeInventory()
                                })
                                {
                                    repeat(6) {
                                        Item(ItemStack(Material.PAPER).editItemMeta {
                                            setCustomModelData(1)
                                            setDisplayName("${ChatColor.GREEN}${ChatColor.BOLD}Enable PvP")
                                        })
                                    }
                                }
                                Grid(3, 2, Modifier.at(5, 1).clickable {
                                    player.playerData.pvpStatus = false
                                    player.error("PvP has been disabled!")
                                    player.closeInventory()
                                }) {
                                    repeat(6) {
                                        Item(ItemStack(Material.PAPER).editItemMeta {
                                            setCustomModelData(1)
                                            setDisplayName("${ChatColor.RED}${ChatColor.BOLD}Disable PvP")
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
