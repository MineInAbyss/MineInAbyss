package com.derongan.minecraft.mineinabyss.ecs

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.world.section.section
import com.derongan.minecraft.mineinabyss.configuration.MIAConfig
import com.derongan.minecraft.mineinabyss.world.Layer
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.entities.parent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.ChatColor.*
import org.bukkit.Location
import org.bukkit.entity.Player

//TODO switch to new Geary ECS
@Serializable
@SerialName("mineinabyss:show_depth")
class AbyssLocationAction : GearyAction() {
    private val GearyEntity.depthMeter by get<DepthMeter>()

    override fun GearyEntity.run(): Boolean {
        val player = parent?.get<Player>() ?: return false
        val sectionXOffset = depthMeter.sectionXOffset
        val sectionYOffset = depthMeter.sectionYOffset
        val abyssStartingHeightInOrth = depthMeter.abyssStartingHeightInOrth
        val section = player.location.section
        val layer: Layer? = section?.layer

        if (layer?.name != null) {
            if (MIAConfig.data.hubSection == WorldManager.getSectionFor(player.location)) {
                player.sendMessage(
                    """
                $DARK_AQUA${ITALIC}The needle spins.
                ${DARK_AQUA}You suddenly become aware that you are in ${layer.name}${DARK_AQUA}.""".trimIndent()
                )
                return true
            }
            if (MIAConfig.data.hubSection != WorldManager.getSectionFor(player.location)){
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
        return true
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

        val numSectionsDeep =
            (location.x / sectionXOffset).toInt() //number of sections under Orth. If in Orth, this should be 0

        return (location.y - abyssStartingHeightInOrth - (numSectionsDeep * sectionYOffset)).toInt()
    }

    private fun pluralizeMeters(count: Int): String {
        val prefix = if (count == 1) "one " else ""
        val suffix = if (count == 1) " block" else " blocks"
        return prefix + -count + suffix
    }
}
