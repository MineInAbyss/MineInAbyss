package com.mineinabyss.relics

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.components.relics.DepthMeter
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.ecs.accessors.EventResultScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.handlers.ComponentAddHandler
import com.mineinabyss.helpers.isInHub
import com.mineinabyss.mineinabyss.core.layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.ChatColor.*
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.roundToInt

@Serializable
@SerialName("mineinabyss:show_depth")
class ShowDepthEvent(
    val depthMeter: DepthMeter
)

class ShowDepthSystem : GearyListener() {
    private val ResultScope.player by get<Player>()

    private inner class ShowDepth : ComponentAddHandler() {
        val EventResultScope.depthMeter by get<ShowDepthEvent>().map { it.depthMeter }

        override fun ResultScope.handle(event: EventResultScope) {
            val sectionXOffset = event.depthMeter.sectionXOffset
            val sectionYOffset = event.depthMeter.sectionYOffset
            val abyssStartingHeightInOrth = event.depthMeter.abyssStartingHeightInOrth
            val section = player.location.section
            val layer: Layer? = section?.layer

            if (layer?.name != null) {
                if (player.isInHub()) {
                    player.sendMessage(
                        """
                $DARK_AQUA${ITALIC}The needle spins.
                ${DARK_AQUA}You suddenly become aware that you are in ${layer.name}${DARK_AQUA}.""".trimIndent()
                    )
                    return
                }
                if (!player.isInHub()) {
                    val depth = getDepth(sectionXOffset, sectionYOffset, abyssStartingHeightInOrth, player.location)
                    player.sendMessage(
                        """
                $DARK_AQUA${ITALIC}The needle spins.
                ${DARK_AQUA}You suddenly become aware that you are in the
                ${layer.name} ${DARK_AQUA}and ${AQUA}${pluralizeMeters(depth)} ${DARK_AQUA}deep into the ${GREEN}Abyss${DARK_AQUA}.
                """.trimIndent()
                    )
                }
            } else player.sendMessage("$ITALIC${DARK_AQUA}The compass wiggles slightly but does not otherwise respond.")
        }
    }

    // TODO memoize total depth of each layer
    // TODO move into an API elsewhere
    /**
     * Calculates the depth of the player in the abyss, in minecraft blocks.
     *
     * @param sectionXOffset                how far apart sections actually are, horizontally
     * @param sectionYOffset                how far apart sections are pretending to be, vertically
     * @param abyssStartingHeightInOrth     at what y value (in Orth) the Depth Meter should say 0. 128 is the big golden bridge
     * @param location                      Location object obtained from Player
     *
     * @return  depth of player in abyss, in minecraft blocks
     */
    private fun getDepth(
        sectionXOffset: Int,
        sectionYOffset: Int,
        abyssStartingHeightInOrth: Int,
        location: Location
    ): Int {

        //number of sections under Orth. If in Orth, this should be 0
        val numSectionsDeep = (location.x / sectionXOffset).roundToInt()

        return (location.y - abyssStartingHeightInOrth - (numSectionsDeep * sectionYOffset)).toInt()
    }

    private fun pluralizeMeters(count: Int): String {
        val prefix = if (count == 1) "one " else ""
        val suffix = if (count == 1) " block" else " blocks"
        return prefix + -count + suffix
    }
}
