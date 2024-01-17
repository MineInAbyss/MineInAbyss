package com.mineinabyss.features.enchants

import com.mineinabyss.features.enchants.enchantments.*
import com.mineinabyss.geary.helpers.component
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.entity.Player

class EnchantsFeature : Feature() {
    override fun FeatureDSL.enable() {
        CustomEnchants.register()

        plugin.listeners(
            SoulBoundListener(),
            FrostAspectListener(),
            BirdSwatterListener(),
            JawBreakerListener(),
            BaneOfKuongatariListener(),
            MagnetismListener(),
        )

        geary.pipeline.addSystems(
            SoulSystem()
        )

        mainCommand {
            "enchant"(desc = "Apply a custom enchantment to an item") {
                permission = "mineinabyss.enchant"
                val options = CustomEnchants.enchantmentList.map { it.key.toString() }
                val availableEnchantment by optionArg(options) {
                    parseErrorMessage = { "No such enchantment: $passed. \nAvailable ones are: \n$options" }
                }
                val enchantmentLevel by intArg { default = 1 }

                playerAction {
                    val player = sender as Player
                    // TODO send error if not found
                    val parsedEnchantClass = serializableComponents.serializers.getClassFor(availableEnchantment)
                    val parsedEnchant = component(parsedEnchantClass).get<CustomEnchants.Type>() ?: return@playerAction
                    val gearyItem = player.inventory.toGeary()?.itemInMainHand ?: return@playerAction

                    val levelRange = (parsedEnchant.minLevel until parsedEnchant.maxLevel + 1)
                    val parsedKey = availableEnchantment

                    if (enchantmentLevel == 0) {
                        CustomEnchants.remove(parsedEnchantClass, gearyItem)
                        sender.success("Removed <b>${parsedKey}</b> from this item.")
                    } else if (enchantmentLevel <= parsedEnchant.maxLevel && enchantmentLevel >= parsedEnchant.minLevel) {
                        if (levelRange.first == levelRange.last)
                            sender.success("Applied <b>${parsedKey}</b> to this item.")
                        else
                            sender.success("Applied <b>${parsedKey} $enchantmentLevel</b> to this item.")
                        CustomEnchants.set(parsedEnchantClass, CustomEnchants.Data(enchantmentLevel), gearyItem)
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
