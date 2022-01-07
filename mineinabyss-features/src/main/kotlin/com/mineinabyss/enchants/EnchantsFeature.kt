package com.mineinabyss.enchants

import com.mineinabyss.enchants.enchantments.*
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

@Serializable
@SerialName("enchants")
class EnchantsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        CustomEnchants.register()

        registerEvents(
            EnchantmentListener(),
            SoulBoundListener(),
            FrostAspectListener(),
            BirdSwatterListener(),
            JawBreakerListener(),
            BaneOfKuongatariListener()
        )

        geary {
            systems(SoulSystem())
        }

        commands {
            mineinabyss {
                "enchant"(desc = "Apply a custom enchantment to an item") {
                    "book"{
                        "rarity"{
                            playerAction {
                                val item = player.inventory.itemInMainHand
                                if (item.type == Material.BOOK) player.inventory.setItemInMainHand(ItemStack(Material.ENCHANTED_BOOK))
                                if (item.type != Material.ENCHANTED_BOOK) return@playerAction
                                val book = item.itemMeta as EnchantmentStorageMeta
                                if (item.type != Material.ENCHANTED_BOOK) {
                                    player.error("Get a book!")
                                    return@playerAction
                                }

                                item.rarity.broadcastVal()
                            }
                        }
                        "add"{
                            val options = CustomEnchants.enchantmentList.map { it.key.toString() }
                            val availableEnchantment by optionArg(options) {
                                parseErrorMessage = { "No such enchantment: $passed. \nAvailable ones are: \n$options" }
                            }
                            val enchantmentLevel by intArg { default = 1 }

                            playerAction {
                                val item = player.inventory.itemInMainHand
                                if (item.type == Material.BOOK) player.inventory.setItemInMainHand(ItemStack(Material.ENCHANTED_BOOK))
                                if (item.type != Material.ENCHANTED_BOOK) return@playerAction
                                val book = item.itemMeta as EnchantmentStorageMeta
                                if (item.type != Material.ENCHANTED_BOOK) {
                                    player.error("Get a book!")
                                    return@playerAction
                                }

                                val parsedEnchant =
                                    CustomEnchants.enchantmentList.firstOrNull {
                                        it.key.toString() == availableEnchantment.lowercase()
                                    } ?: (command.stopCommand(""))

                                val levelRange =
                                    (parsedEnchant.startLevel until parsedEnchant.maxLevel + 1)

                                if (enchantmentLevel == 0) {

                                    book.removeStoredEnchant(parsedEnchant)
                                    sender.success("Removed ${ChatColor.BOLD}${parsedEnchant.name} ${ChatColor.GREEN}from this item.")
                                    return@playerAction
                                }
                                if (enchantmentLevel <= parsedEnchant.maxLevel && enchantmentLevel >= parsedEnchant.startLevel) {
                                    if (levelRange.first == levelRange.last) sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} ${ChatColor.GREEN}to this item.")
                                    else sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} $enchantmentLevel ${ChatColor.GREEN}to this item.")
                                    book.removeStoredEnchant(parsedEnchant)
                                    book.addStoredEnchant(parsedEnchant, enchantmentLevel, false)

                                }
                                if (enchantmentLevel > levelRange.last) command.stopCommand("Level exceeds this enchantments max level.")
                                item.itemMeta = book
                                broadcast("i did it")
                                item.updateEnchantmentLore(parsedEnchant as EnchantmentWrapper, enchantmentLevel)

                            }
                        }
                        "fetch" {
                            playerAction {
                                val item = player.inventory.itemInMainHand
                                if (item.type == Material.BOOK) player.inventory.setItemInMainHand(ItemStack(Material.ENCHANTED_BOOK))
                                if (item.type != Material.ENCHANTED_BOOK) {
                                    player.error("Get a book!")
                                    return@playerAction
                                }

                                val book = item.itemMeta as EnchantmentStorageMeta
                                if (book.storedEnchants.isEmpty()) player.error("no enchantment")
                                book.storedEnchants.forEach {
                                    it.broadcastVal("Enchantment: ")

                                }
                            }
                        }
                    }
                    "apply"{
                        permission = "mineinabyss.enchant"
                        val options = CustomEnchants.enchantmentList.map { it.key.toString() }
                        val availableEnchantment by optionArg(options) {
                            parseErrorMessage = { "No such enchantment: $passed. \nAvailable ones are: \n$options" }
                        }
                        val enchantmentLevel by intArg { default = 1 }

                        playerAction {
                            val item = player.inventory.itemInMainHand
                            val parsedEnchant =
                                CustomEnchants.enchantmentList.firstOrNull {
                                    it.key.toString() == availableEnchantment.lowercase()
                                } ?: (command.stopCommand(""))

                            val levelRange =
                                (parsedEnchant.startLevel until parsedEnchant.maxLevel + 1)

                            if (enchantmentLevel == 0) {

                                item.removeCustomEnchant(parsedEnchant as EnchantmentWrapper)
                                sender.success("Removed ${ChatColor.BOLD}${parsedEnchant.name} ${ChatColor.GREEN}from this item.")
                            }
                            if (enchantmentLevel <= parsedEnchant.maxLevel && enchantmentLevel >= parsedEnchant.startLevel) {
                                if (levelRange.first == levelRange.last) sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} ${ChatColor.GREEN}to this item.")
                                else sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} $enchantmentLevel ${ChatColor.GREEN}to this item.")
                                item.addCustomEnchant(parsedEnchant as EnchantmentWrapper, enchantmentLevel)
                            }
                            if (enchantmentLevel > levelRange.last) command.stopCommand("Level exceeds this enchantments max level.")
                            item.updateEnchantmentLore(parsedEnchant as EnchantmentWrapper, enchantmentLevel)
                        }
                    }
                    "get"{
                        playerAction {
                            broadcast(player.inventory.itemInMainHand.enchantments)
                        }
                    }
                }
            }

            val enchants  = CustomEnchants.enchantmentList
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "enchant"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "enchant" -> listOf("apply", "book", "get")
                            else -> null
                        }
                    }
                    3 -> {
                        when (args[1]) {
                            "apply" -> enchants.map { it.key.toString() }
                            "book" -> listOf("add", "fetch")
                            else -> null
                        }
                    }
                    4 -> {
                        when (args[0]) {
                            "enchant" ->
                                ((enchants.find { it.key.toString() == args[1] }?.startLevel)?.rangeTo
                                    ((enchants.find { it.key.toString() == args[1] }!!.maxLevel)))?.map { it.toString() }
                        }
                        when (args[3]) {
                            "add" ->
                                ((enchants.find { it.key.toString() == args[1] }?.startLevel)?.rangeTo
                                    ((enchants.find { it.key.toString() == args[1] }!!.maxLevel)))?.map { it.toString() }
                            else -> null
                        }

                    }
                    else -> null
                }
            }
        }
    }
}
