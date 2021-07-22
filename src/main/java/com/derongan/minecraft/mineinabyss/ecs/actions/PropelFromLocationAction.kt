package com.derongan.minecraft.mineinabyss.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

class PropelFromLocationAction (
    private val power: Double,
    private val location: Vector,  // Not sure if this should be a Vector or Location
    private val scaleWithDistance: Boolean,
    private val cancelCurrentVelocity: Boolean
) : GearyAction() {
    val GearyEntity.entity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        var velocity = location.subtract(entity.location.toVector())
        velocity = velocity.normalize()
        var distance = location.distance(entity.location.toVector())

        // This is wacky placeholder math, probably change it at some point
        val maxForce = (power * 5.0)
        var scalar: Double
        if(scaleWithDistance) {
            if(distance == 0.0) {
                scalar = maxForce
            }
            else {
                scalar = power / (distance * distance * distance)
                if(scalar > maxForce)
                    scalar = maxForce
            }
        } else
            scalar = power

        velocity.multiply(scalar)

        if(cancelCurrentVelocity)
            entity.velocity = velocity
        else
            entity.velocity.add(velocity)

        return true
    }

}
