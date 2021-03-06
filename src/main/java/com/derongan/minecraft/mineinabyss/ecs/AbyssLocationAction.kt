package com.derongan.minecraft.mineinabyss.ecs

import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.deeperworld.world.section.section
import com.derongan.minecraft.mineinabyss.world.Layer
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.components.parent
import com.mineinabyss.geary.minecraft.components.toBukkit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.ChatColor.*
import org.bukkit.Location
import org.bukkit.entity.Player

//TODO switch to new Geary ECS
@Serializable
@SerialName("mineinabyss:show_depth")
class AbyssLocationAction : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        val (accuracy) = entity.get<DepthMeter>() ?: return false
        val player = entity.parent?.toBukkit<Player>() ?: return false

        val section = player.location.section
        val layer: Layer? = section?.layer

        if (layer == null) {
            player.sendMessage("$ITALIC${DARK_AQUA}The compass wiggles slightly but does not otherwise respond.")
        } else {
            val depth = accuracy * (getDepth(layer, section, player.location) / accuracy)
            player.sendMessage(
                """
                $ITALIC${DARK_AQUA}The compass spins.
                You are suddenly aware that you are about $AQUA${pluralizeMeters(depth)} $DARK_AQUA deep in $AQUA${layer.name}.
                """.trimIndent()
            )
        }
        return true
    }

    // TODO memoize total depth of each layer
    // TODO move into an API elsewhere
    private fun getDepth(layer: Layer, section: Section, location: Location): Int {
        var totalDepth = 0
        var currentSectionTop = 0
        val numSections = layer.sections.size
        layer.sections.forEachIndexed { index, s ->
            run {
                if (section == s) {
                    currentSectionTop = totalDepth
                }
                totalDepth += if (index != numSections - 1) {
                    s.referenceBottom.y.toInt()
                } else {
                    // This isn't totally accurate since there is overlap with the next layer, but 256 is a good best guess.
                    256
                }
            }
        }

        val minecraftDepth = currentSectionTop + (256 - location.y)
        val layerDepth = layer.endDepth - layer.startDepth

        return (layer.startDepth + minecraftDepth / totalDepth * layerDepth).toInt()
            .coerceIn(layer.startDepth, layer.endDepth)
    }

    private fun pluralizeMeters(count: Int): String {
        val prefix = if (count == 1) "one " else ""
        val suffix = if (count == 1) " meter" else " meters"
        return prefix + count + suffix
    }
}
