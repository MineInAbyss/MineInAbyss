package com.derongan.minecraft.mineinabyss.geary

import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.deeperworld.world.section.section
import com.derongan.minecraft.mineinabyss.world.Layer
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.geary.ecs.Family
import com.mineinabyss.geary.ecs.component.components.control.Activated
import com.mineinabyss.geary.ecs.entity.GearyEntity
import com.mineinabyss.geary.ecs.system.IteratingSystem
import org.bukkit.ChatColor
import org.bukkit.Location

//TODO switch to new Geary ECS
class AbyssLocationSystem : IteratingSystem(
    Family.builder().setAll(setOf(Activated::class.java, DepthMeter::class.java)).build()
) {

    override fun update(gearyEntity: GearyEntity?) {
        val entity = gearyEntity!!

        entity.holdingPlayer.ifPresent { player ->
            val section = player.location.section
            val layer: Layer? = section?.layer

            if (layer == null) {
                player.sendMessage("${ChatColor.ITALIC}${ChatColor.DARK_AQUA}The compass wiggles slightly but does not otherwise respond.")
            } else {
                val accuracy = entity.getComponent(DepthMeter::class.java).accuracy
                val depth = accuracy * (getDepth(layer, section, player.location) / accuracy)
                player.sendMessage(
                    "${ChatColor.ITALIC}${ChatColor.DARK_AQUA}The compass spins. You are suddenly aware that you are about ${ChatColor.AQUA}${
                        pluralizeMeters(
                            depth
                        )
                    }${ChatColor.DARK_AQUA} deep in ${ChatColor.AQUA}${layer.name}."
                )
            }
        }

        entity.removeComponent(Activated::class.java)
    }

    // TODO memoize total depth of each layer
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

    fun pluralizeMeters(count: Int): String {
        val prefix = if (count == 1) "one " else ""
        val suffix = if (count == 1) " meter" else " meters"
        return prefix + count + suffix
    }
}
