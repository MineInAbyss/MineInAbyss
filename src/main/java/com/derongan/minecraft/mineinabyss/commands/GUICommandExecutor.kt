package com.derongan.minecraft.mineinabyss.commands

import com.derongan.minecraft.mineinabyss.abyssContext
import com.derongan.minecraft.mineinabyss.gui.GondolaGUI
import com.derongan.minecraft.mineinabyss.gui.StatsGUI
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.playerData
import com.mineinabyss.idofront.commands.Command.PlayerExecution
import com.mineinabyss.idofront.commands.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.arguments.IntArg
import com.mineinabyss.idofront.commands.arguments.StringArg
import com.mineinabyss.idofront.commands.onExecuteByPlayer
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

class GUICommandExecutor : IdofrontCommandExecutor() {
    private val leaveConfirm = ArrayList<UUID>()

    private val PlayerExecution.playerData get() = player.playerData

    override val commands = commands(mineInAbyss) {
        command(CommandLabels.STATS) {
            onExecuteByPlayer {
                StatsGUI(player).show(player)
            }
        }
        command(CommandLabels.START) {
            onExecuteByPlayer {
                if (playerData.isIngame) {
                    sender.error("You are already ingame!\nYou can leave using /stopdescent")
                    return@onExecuteByPlayer
                }
                GondolaGUI(player).show(player)
            }
        }
        command(CommandLabels.STOP_DESCENT) {
            onExecuteByPlayer {
                if (!playerData.isIngame) {
                    sender.error("You are not currently ingame!\nStart by using /start")
                } else if (!leaveConfirm.contains(player.uniqueId)) {
                    leaveConfirm.add(player.uniqueId)
                    sender.info("""
                                &cYou are about to leave the game!!!
                                &lYour progress will be lost&r&c, but any xp and money you earned will stay with you.
                                Type /stopdescent again to leave""".trimIndent().color())
                } else {
                    leaveConfirm.remove(player.uniqueId)
                    player.health = 0.0
                }
            }
        }
        command(CommandLabels.CREATE_GONDOLA_SPAWN) {
            val displayName by +StringArg("display name") { default = "" }
            val cost by +IntArg("cost") { default = 0 }

            onExecuteByPlayer {
                val spawnLocConfig = abyssContext.configManager.startLocationCM
                val spawns = spawnLocConfig.getMapList(GondolaGUI.SPAWN_KEY)
                var displayItem = player.inventory.itemInMainHand.clone()

                if (displayItem.type == Material.AIR)
                    displayItem = ItemStack(Material.GRASS_BLOCK)

                displayItem.editItemMeta {
                    if (displayName != "")
                        setDisplayName(displayName)
                    addItemFlags(ItemFlag.HIDE_ATTRIBUTES) //TODO probably better to add these tags to the serialized items
                    addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                }

                val map = HashMap<String, Any>()
                map["location"] = player.location
                map["display-item"] = displayItem
                if (cost != 0)
                    map["cost"] = cost

                spawns.add(map)
                spawnLocConfig.set(GondolaGUI.SPAWN_KEY, spawns)
                spawnLocConfig.saveConfig()

                sender.success("Created spawn")
            }
        }
    }
}
