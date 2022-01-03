package com.mineinabyss.enchants

import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.ChatColor

@Serializable
@SerialName("enchants")
class EnchantsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        CustomEnchants.register()

        registerEvents(
            SoulBoundListener(),
            FrostAspectListener()
        )

        geary {
            systems(SoulSystem())
        }

        commands {
            mineinabyss {
                "enchant"(desc = "Apply a custom enchantment to an item") {
                    permission = "mineinabyss.enchant"
                    val options = CustomEnchants.enchantmentList.map { it.key.toString() }
                    val availableEnchantment by optionArg(options) {
                        parseErrorMessage = { "No such enchantment: $passed. \nAvailable ones are: \n$options" }
                    }
                    val enchantmentLevel by intArg { default = 1 }

                    playerAction {
                        val parsedEnchant =
                            CustomEnchants.enchantmentList.firstOrNull {
                                it.key.toString() == availableEnchantment.lowercase()
                            } ?: (command.stopCommand(""))

                        val levelRange =
                            (parsedEnchant.startLevel until parsedEnchant.maxLevel + 1)

                        if (enchantmentLevel == 0) {

                            player.inventory.itemInMainHand.removeCustomEnchant(parsedEnchant)
                            sender.success("Removed ${ChatColor.BOLD}${parsedEnchant.name} ${ChatColor.GREEN}from this item.")
                        }
                        if (enchantmentLevel <= parsedEnchant.maxLevel && enchantmentLevel >= parsedEnchant.startLevel) {
                            if (levelRange.first == levelRange.last) sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} ${ChatColor.GREEN}to this item.")
                            else sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} $enchantmentLevel ${ChatColor.GREEN}to this item.")
                            player.inventory.itemInMainHand.addCustomEnchant(
                                parsedEnchant as EnchantmentWrapper,
                                enchantmentLevel
                            )
                        }
                        if (enchantmentLevel > levelRange.last) command.stopCommand("Level exceeds this enchantments max level.")
                    }
                }
            }

            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "enchant"
                    ).filter { it.startsWith(args[0]) }
                    2 -> CustomEnchants.enchantmentList.map { it.key.toString() }
                    3 -> {
                        val enchant = CustomEnchants.enchantmentList.find { it.key.toString() == args[0] }
                        ((enchant?.startLevel ?: 0)..(enchant?.maxLevel ?: 0)).map { it.toString() }
                    }
                    else -> listOf()
                }
            }
        }
    }
}
