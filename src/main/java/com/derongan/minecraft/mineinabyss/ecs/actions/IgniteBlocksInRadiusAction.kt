package com.derongan.minecraft.mineinabyss.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTag
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class IgniteBlocksInRadiusAction(
    private val radius: Double  // Again not really a radius but close enough
) : GearyAction() {
    val GearyEntity.entity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        val intRadius = radius as Int
        for (x in -intRadius..intRadius) {
            for (y in -intRadius..intRadius) {
                for (z in -intRadius..intRadius) {
                    val location = Location(
                        entity.world,
                        entity.location.x + x,
                        entity.location.y + y,
                        entity.location.z + z
                    )
                    if (location.block.isEmpty) {
                        val belowLocation = Location(
                            location.world,
                            location.x,
                            location.y + 1,
                            location.z
                        )
                        if (
                            !(belowLocation.block.isEmpty ||
                                    belowLocation.block.isLiquid)
                        )
                            location.block.type = Material.FIRE
                    }
                }
            }
        }
        return true
    }

}
