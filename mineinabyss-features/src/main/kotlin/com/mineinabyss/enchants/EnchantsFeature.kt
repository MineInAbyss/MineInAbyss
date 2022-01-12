package com.mineinabyss.enchants

import com.mineinabyss.enchants.enchantments.*
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
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
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import kotlin.random.Random

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
                permission = "mineinabyss.enchant"
                "enchant"(desc = "Apply a custom enchantment to an item") {
                    "book"{
                        "add"{
                            val options = CustomEnchants.enchantmentList.map { it.key.toString() }
                            val availableEnchantment by optionArg(options) {
                                parseErrorMessage = { "No such enchantment: $passed. \nAvailable ones are: \n$options" }
                            }
                            val enchantmentLevel by intArg { default = 1 }

                            playerAction {
                                val item = player.inventory.itemInMainHand
                                if (item.type == Material.BOOK) player.inventory.setItemInMainHand(ItemStack(Material.ENCHANTED_BOOK))
                                if (player.inventory.itemInMainHand.type != Material.ENCHANTED_BOOK) {
                                    player.error("Get a book!")
                                    return@playerAction
                                }
                                val book = player.inventory.itemInMainHand.itemMeta as EnchantmentStorageMeta

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
                                    book.removeStoredEnchant(parsedEnchant)
                                    item.removeCustomEnchant(parsedEnchant as EnchantmentWrapper)
                                    book.addStoredEnchant(parsedEnchant, enchantmentLevel, false)
                                    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, Random.nextFloat(), Random.nextFloat())
                                    if (levelRange.first == levelRange.last) sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} ${ChatColor.GREEN}to this item.")
                                    else sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} $enchantmentLevel ${ChatColor.GREEN}to this item.")

                                }
                                if (enchantmentLevel > levelRange.last) command.stopCommand("Level exceeds this enchantments max level.")
                                player.inventory.itemInMainHand.itemMeta = book
                                player.inventory.itemInMainHand.updateEnchantmentLore(parsedEnchant as EnchantmentWrapper, enchantmentLevel)

                            }
                        }
                        "check" {
                            playerAction {
                                val item = player.inventory.itemInMainHand
                                if (item.type == Material.BOOK) player.inventory.setItemInMainHand(ItemStack(Material.ENCHANTED_BOOK))
                                if (player.inventory.itemInMainHand.type != Material.ENCHANTED_BOOK) {
                                    player.error("Get a book!")
                                    return@playerAction
                                }

                                val book = player.inventory.itemInMainHand.itemMeta as EnchantmentStorageMeta
                                if (book.storedEnchants.isEmpty()) player.error("Book has no enchantments")
                                else {
                                    player.success("Enchantments: ")
                                    book.storedEnchants.forEach {
                                        player.info("${it.key.name} ${convertEnchantmentLevel(it.value)}")
                                    }
                                }
                            }
                        }
                    }
                    "item" {
                        "add"{
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

                                val target = getItemTarget(item)

                                if ((!getEnchantmentTarget(parsedEnchant).contains(target) || (parsedEnchant as EnchantmentWrapper).allowedItems.contains(target)) && !player.hasPermission("mineinabyss.enchant.allowunsafe")) {
                                    player.error("This enchantment cannot be applied to this weapon.")
                                    player.error("To do so, you need the permission: ${ChatColor.ITALIC}mineinabyss.enchant.allowunsafe")
                                    return@playerAction
                                }

                                if (enchantmentLevel == 0) {

                                    item.removeCustomEnchant(parsedEnchant as EnchantmentWrapper)
                                    sender.success("Removed ${ChatColor.BOLD}${parsedEnchant.name} ${ChatColor.GREEN}from this item.")
                                }
                                if (enchantmentLevel <= parsedEnchant.maxLevel && enchantmentLevel >= parsedEnchant.startLevel) {
                                    player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, Random.nextFloat(), Random.nextFloat())
                                    item.removeCustomEnchant(parsedEnchant as EnchantmentWrapper)
                                    item.addCustomEnchant(parsedEnchant, enchantmentLevel)
                                    if (levelRange.first == levelRange.last) sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} ${ChatColor.GREEN}to this item.")
                                    else sender.success("Applied ${ChatColor.BOLD}${parsedEnchant.name} $enchantmentLevel ${ChatColor.GREEN}to this item.")
                                }
                                item.updateEnchantmentLore(parsedEnchant as EnchantmentWrapper, enchantmentLevel)
                            }
                        }
                        "check"{
                            playerAction {
                                broadcast(player.inventory.itemInMainHand.enchantments)
                            }
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
                            "enchant" -> listOf("item", "book")
                            else -> null
                        }
                    }
                    3 -> {
                        when (args[1]) {
                            "item" -> listOf("add", "check")
                            "book" -> listOf("add", "check")
                            else -> null
                        }
                    }
                    4 -> {
                        when (args[2]) {
                            "add" -> enchants.map { it.key.toString() }
                            else -> null
                        }

                    }
                    else -> null
                }
            }
        }
    }
}
