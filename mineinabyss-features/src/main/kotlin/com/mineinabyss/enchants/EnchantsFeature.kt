package com.mineinabyss.enchants

import com.mineinabyss.enchants.enchantments.*
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("enchants")
class EnchantsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        CustomEnchants.register()

        listeners(
            SoulBoundListener(),
            FrostAspectListener(),
            BirdSwatterListener(),
            JawBreakerListener(),
            BaneOfKuongatariListener(),
            MagnetismListener(),
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
                        val player = sender as Player
                        val parsedEnchant =
                            CustomEnchants.enchantmentList.firstOrNull {
                                it.key.toString().lowercase() == availableEnchantment.lowercase()
                            } ?: (command.stopCommand(""))

                        val levelRange = (parsedEnchant.startLevel until parsedEnchant.maxLevel + 1)
                        val parsedKey = parsedEnchant.key.key

                        if (enchantmentLevel == 0) {
                            player.inventory.itemInMainHand.removeCustomEnchant(parsedEnchant)
                            sender.success("Removed <b>${parsedKey}</b> from this item.")
                        } else if (enchantmentLevel <= parsedEnchant.maxLevel && enchantmentLevel >= parsedEnchant.startLevel) {
                            if (levelRange.first == levelRange.last)
                                sender.success("Applied <b>${parsedKey}</b> to this item.")
                            else
                                sender.success("Applied <b>${parsedKey} $enchantmentLevel</b> to this item.")

                            player.inventory.itemInMainHand.addCustomEnchant(
                                parsedEnchant as EnchantmentWrapper,
                                enchantmentLevel
                            )
                        }

                        if (enchantmentLevel > levelRange.last)
                            command.stopCommand("Level exceeds this enchantments max level.")
                    }
                }
            }

            val enchants = CustomEnchants.enchantmentList
            tabCompletion {
                when (args.size) {
                    1 -> listOf("enchant").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "enchant" -> enchants.map { it.key.toString() }
                            else -> null
                        }
                    }
                    3 -> {
                        when (args[0]) {
                            "enchant" ->
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
