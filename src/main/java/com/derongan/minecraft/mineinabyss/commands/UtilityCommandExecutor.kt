package com.derongan.minecraft.mineinabyss.commands

import com.derongan.minecraft.mineinabyss.enumValueOfOrNull
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.services.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.systems.CustomEnchants
import com.derongan.minecraft.mineinabyss.systems.EnchantmentWrapper
import com.derongan.minecraft.mineinabyss.systems.addCustomEnchant
import com.derongan.minecraft.mineinabyss.systems.removeCustomEnchant
import com.derongan.minecraft.mineinabyss.world.Layer
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.okkero.skedule.BukkitSchedulerController
import com.okkero.skedule.schedule
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

@ExperimentalCommandDSL
object UtilityCommandExecutor : IdofrontCommandExecutor(), TabCompleter {
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

        "clearcontainers"{
            val itemToClear by stringArg()
            val layerToClear by stringArg { default = "all" }

            playerAction {
                val parsedItem = enumValueOfOrNull<Material>(itemToClear) ?: command.stopCommand("Item not found")

                if (layerToClear == "all") {
                    sender.info("Start clearing out $parsedItem from containers in all layers.")
                    AbyssWorldManager.layers.forEach {
                        mineInAbyss.schedule {
                            clearItemFromContainers(it, parsedItem, sender)
                        }
                    }
                } else {
                    val parsedLayer =
                        AbyssWorldManager.layers.firstOrNull {
                            it.name == layerToClear
                        } ?: command.stopCommand("Layer not found")
                    sender.info("Start clearing out $parsedItem from containers in ${parsedLayer.name}.")
                    mineInAbyss.schedule {
                        clearItemFromContainers(parsedLayer, parsedItem, sender)
                    }
                }
            }
        }
    }

    private suspend fun BukkitSchedulerController.clearItemFromContainers(
        layer: Layer,
        item: Material,
        sender: CommandSender
    ) {
        repeating(1)

        val worldsToBeChecked = layer.sections.groupBy { it.world }
        worldsToBeChecked.forEach { (world, sections) ->
            sections.forEach { section ->
                for (x in (section.region.a.x / 16)..(section.region.b.x / 16)) {
                    for (z in (section.region.a.z / 16)..(section.region.b.z / 16)) {
                        val chunk = world.getChunkAt(x, z)
                        chunk.load()
                        val containers = chunk.tileEntities
                            .filterIsInstance<Container>()
                            .filter { section.region.contains(it.location.x.toInt(), it.location.z.toInt()) }

                        containers.forEach { container ->
                            if (container.inventory.contains(item)) {
                                val numberOfItems = container.inventory.contents
                                    .filter { it != null && it.type == item }
                                    .sumOf { it.amount }
                                container.inventory.remove(item)
                                sender.info("Removed $numberOfItems ${item.name} from a container at x:${container.x} y:${container.y} z:${container.z} in layer ${layer.name} (world ${world.name})")
                            }
                        }
                        chunk.unload()
                        yield()
                    }
                }
            }
        }
        sender.info("Finished layer ${layer.name}")
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