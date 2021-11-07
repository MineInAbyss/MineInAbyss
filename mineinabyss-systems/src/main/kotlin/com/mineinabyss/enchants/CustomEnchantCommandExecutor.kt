package com.mineinabyss.enchants

import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.mineInAbyss
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CustomEnchantCommandExecutor : IdofrontCommandExecutor(), TabCompleter {
    override val commands = commands(mineInAbyss) {
        "abyssenchant"{
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

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        if (command.name != "abyssenchant") return emptyList()
        return when (args.size) {
            1 -> CustomEnchants.enchantmentList.map { it.key.toString() }
            2 -> {
                val enchant = CustomEnchants.enchantmentList.find { it.key.toString() == args[0] }
                ((enchant?.startLevel ?: 0)..(enchant?.maxLevel ?: 0)).map { it.toString() }
            }
            else -> emptyList()
        }
    }
}
