package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.features.helpers.layer
import com.mineinabyss.features.hubstorage.isInHub
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.Location
import org.bukkit.entity.Player


object ShowDepthSystem {
    fun register(world: Geary) = world.observe<ShowDepth>().exec(world.query<Player>()) { (player) ->
        if (entity.has<ShowDepthMeterHud>()) return@exec
        player.sendDepthMessage()
    }

    fun Player.sendDepthMessage() {
        location.section?.layer?.let { layer ->
            location.getAbyssDepth()?.let { depth ->
                info(
                    """
                    <dark_aqua><i>The needle spins.</i>
                    You suddenly become aware that you are in """.trimIndent().miniMsg().append(
                        (if (isInHub()) "${layer.name}<dark_aqua>.".trimIndent()
                        else "${layer.name}<dark_aqua> and <aqua>${pluralizeMeters(depth)}</aqua> deep into the <green>Abyss</green>.").trimIndent()
                            .miniMsg()
                    )
                )
            }
        } ?: info("<i><dark_aqua>The compass wiggles slightly but does not otherwise respond.")
    }

    private fun pluralizeMeters(count: Int): String {
        val suffix = if (count == 1) " block" else " blocks"
        return "${count}$suffix"
    }
}

/**
 * Gets the abyss depth for the given location
 *
 * @return The abyss depth of the given location in blocks, or null if location is not in a managed section
 */
fun Location.getAbyssDepth(): Int? {
    return WorldManager.getDepthFor(this)?.let { depth ->
        depth - (world.maxHeight - Features.layers.config.hubSection.region.min.y - 1 /* Always start at depth 1 */)
    }
}