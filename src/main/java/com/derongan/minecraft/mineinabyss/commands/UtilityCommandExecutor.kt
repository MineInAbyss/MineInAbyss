package com.derongan.minecraft.mineinabyss.commands

import com.derongan.minecraft.mineinabyss.enumValueOfOrNull
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.services.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.world.Layer
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.info
import com.okkero.skedule.BukkitSchedulerController
import com.okkero.skedule.schedule
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.command.CommandSender

@ExperimentalCommandDSL
object UtilityCommandExecutor : IdofrontCommandExecutor() {
    override val commands = commands(mineInAbyss) {
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
                for (x in (section.region.a.x / 16)..(section.region.b.x / 16)){
                    for(z in (section.region.a.z / 16)..(section.region.b.z / 16)){
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
}