package com.derongan.minecraft.mineinabyss.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity

/**
 * Self destructs (makes an explosion and kills) an entity
 */
class SelfDestructAction(
    private val explosionPower: Double,
    private val setFire: Boolean
) : GearyAction() {
    val GearyEntity.entity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        entity.world.createExplosion(entity.location, explosionPower as Float, setFire)
        if(entity is Damageable)
            (entity as Damageable).health = 0.0
        return true
    }
}