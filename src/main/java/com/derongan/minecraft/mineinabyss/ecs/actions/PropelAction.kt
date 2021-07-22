package com.derongan.minecraft.mineinabyss.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class PropelActionAngle(
    private val power: Double,
    private val angleX: Double,  // 0 is forward
    private val angleY: Double,   // 0 is forward, - is down, + is up
    private val cancelCurrentVelocity: Boolean
) : GearyAction() {
    val GearyEntity.entity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        var velocity = Vector(
            power * cos(angleY) * sin(angleX),
            power * cos(angleY) * cos(angleX),
            power * sin(angleY)
        )
        if (cancelCurrentVelocity)
            entity.velocity = velocity;
        else
            entity.velocity.add(velocity);

        return true
    }

}

class PropelActionVector(
    private val force: Vector,
    private val cancelCurrentVelocity: Boolean
) : GearyAction() {
    val GearyEntity.entity by get<Entity>()

    override fun GearyEntity.run(): Boolean {
        if (cancelCurrentVelocity)
            entity.velocity = force;
        else
            entity.velocity.add(force);

        return true
    }

}
