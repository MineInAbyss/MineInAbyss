package com.derongan.minecraft.mineinabyss.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.access.geary
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

class AreaDamageAction (
    private val damage: Double,
    private val radius: Double,  // Not really radius as it's a box but whatever
        ) : GearyAction() {
    val GearyEntity.entity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        for(targetEntity in entity.getNearbyEntities(radius, radius, radius)) {
            if(targetEntity is LivingEntity) {
                (targetEntity as LivingEntity).damage(damage)
            }
        }
        return true
    }

}

class AreaDamageWithKnockBackAction (
    private val damage: Double,
    private val radius: Double,  // Not really radius as it's a box but whatever
    private val knockBackPower: Double,
    private val knockBackYAngle: Double,
    private val scaleKnockBackWithDistance: Boolean
) : GearyAction() {
    val GearyEntity.entity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        for(targetEntity in entity.getNearbyEntities(radius, radius, radius)) {
            if(targetEntity is LivingEntity) {
                (targetEntity as LivingEntity).damage(damage)
                val knockBackAction = KnockBackFromLocationAction (
                    knockBackPower,
                    knockBackYAngle,
                    entity.location.toVector(),
                    scaleKnockBackWithDistance,
                    false)
                knockBackAction.runOn(geary(targetEntity))
            }
        }
        return true
    }

}