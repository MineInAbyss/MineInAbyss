package com.derongan.minecraft.mineinabyss.ecs.actions

import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.okkero.skedule.schedule
import org.bukkit.entity.Entity

/**
 * Strikes an entity with lightning bolts
 */
class SmiteAction(
    private val strikeCount: Int,
    private val timeBetween: Double
) : GearyAction() {
    val GearyEntity.entity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        mineInAbyss.schedule {
            for(i in 0..strikeCount) {
                entity.world.strikeLightning(entity.location)
                waitFor((timeBetween * 20) as Long)
            }
        }
        return true
    }
}
