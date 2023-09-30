package com.mineinabyss.features.relics.depthmeter

import com.mineinabyss.components.relics.DepthMeter
import com.mineinabyss.components.relics.ShowDepthMeterHud
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.features.hubstorage.isInHub
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.roundToInt

@Serializable
@SerialName("mineinabyss:show_depth")
class ShowDepth

class ShowDepthSystem : GearyListener() {
    private val Pointers.player by get<Player>().on(target)
    private val Pointers.depthMeter by get<DepthMeter>().on(source)
    private val Pointers.hasDepth by family { has<ShowDepth>() }.on(event)

    override fun Pointers.handle() {
        if (player.toGeary().has<ShowDepthMeterHud>()) return
        player.sendDepthMessage()
    }

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
    fun calculateDepth(
        sectionXOffset: Int,
        sectionYOffset: Int,
        abyssStartingHeightInOrth: Int,
        location: Location
    ): Int {

        //number of sections under Orth. If in Orth, this should be 0
        val numSectionsDeep = (location.x / sectionXOffset).roundToInt()

        return -(location.y - abyssStartingHeightInOrth - (numSectionsDeep * sectionYOffset)).toInt()
    }

    fun pluralizeMeters(count: Int): String {
        val prefix = if (count == 1) "one " else ""
        val suffix = if (count == 1) " block" else " blocks"
        return "$prefix${count}$suffix"
    }

    fun Player.sendDepthMessage() {
        val layer = location.section?.layer
        if (layer?.name != null) {
            info(
                """
                    <dark_aqua><i>The needle spins.</i>
                    You suddenly become aware that you are in """.trimIndent().miniMsg().append(
                    (if (isInHub()) "${layer.name}<dark_aqua>.".trimIndent()
                    else "${layer.name}<dark_aqua> and <aqua>${pluralizeMeters(getDepth())}</aqua> deep into the <green>Abyss</green>.").trimIndent().miniMsg())
            )
        } else info("<i><dark_aqua>The compass wiggles slightly but does not otherwise respond.")
    }
}

fun Location.getDepth(depthMeter: DepthMeter = DepthMeter()): Int {
    return ShowDepthSystem().calculateDepth(
        depthMeter.sectionXOffset,
        depthMeter.sectionYOffset,
        depthMeter.abyssStartingHeightInOrth,
        this
    )
}

fun Player.getDepth(): Int = location.getDepth()
