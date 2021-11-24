package com.mineinabyss.relics

import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyHandlerScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.minecraft.actions.Explosion
import com.mineinabyss.geary.minecraft.events.onItemLeftClick
import com.mineinabyss.geary.minecraft.location.ConfigurableTargetLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:blaze_reap")
class BlazeReap(
    val explosion: Explosion,
    val at: ConfigurableTargetLocation,
)

class BlazeReapBehaviour : GearyListener() {
    private val ResultScope.blazeReap by get<BlazeReap>()

    override fun GearyHandlerScope.register() {
        onItemLeftClick { event ->
//            entity.explode(blazeReap.explosion, at = event.player.atTargetBlock(blazeReap.at))
        }
    }
}
