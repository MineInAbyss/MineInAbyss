package com.mineinabyss.cosmetics

import com.mineinabyss.helpers.*
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("cosmetics")
class CosmeticsFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(CosmeticListener())
        commands {
            mineinabyss {
                "cosmetic" {
                    "menu" {
                        ensureSenderIsPlayer()
                        playerAction {
                            hmcCosmetics.cosmeticsMenu.openDefault(sender as Player)
                        }
                    }
                    "equip" {
                        "hat" {
                            val hat by stringArg()
                            playerAction {
                                (sender as Player).equipCosmeticHat(hat)
                            }
                        }
                        "backpack" {
                            val backpack by stringArg()
                            playerAction {
                                (sender as Player).equipCosmeticBackPack(backpack)
                            }
                        }
                    }
                    "unequip" {
                        val type by stringArg()
                        playerAction {
                            when (type) {
                                "hat" -> (sender as Player).unequipCosmeticHat()
                                "backpack" -> (sender as Player).unequipCosmeticBackpack()
                            }
                        }
                    }
                    "gesture" {
                        val gesture by stringArg()
                        playerAction {
                            (sender as Player).playGesture(gesture)
                        }
                    }
                }
            }
            tabCompletion {
                val emotes: MutableList<String> = ArrayList()
                val hats: MutableList<String> = ArrayList()
                val backpacks: MutableList<String> = ArrayList()

                for (gesture in mcCosmetics.gestureManager.allCosmetics) {
                    if ((this.sender as Player).hasPermission(gesture.permission))
                        emotes.add(gesture.key)
                }

                when (args.size) {
                    1 -> listOf("cosmetic").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "cosmetic" -> listOf("menu", "equip", "unequip", "gesture").filter { it.startsWith(args[1]) }
                            else -> listOf()
                        }
                    }
                    3 -> {
                        when (args[1]) {
                            "gesture" -> emotes.filter { it.startsWith(args[2]) }
                            "equip" -> listOf("hat", "backpack")
                            "unequip" -> listOf("hat", "backpack")
                            else -> listOf()
                        }
                    }
                    4 -> {
                        when (args[2]) {
                            "hat" -> hats.filter { it.startsWith(args[3]) }
                            "backpack" -> backpacks.filter { it.startsWith(args[3]) }
                            else -> listOf()
                        }
                    }
                    else -> listOf()
                }
            }
        }
    }
}
