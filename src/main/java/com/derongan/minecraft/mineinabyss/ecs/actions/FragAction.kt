package com.derongan.minecraft.mineinabyss.ecs.actions

import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.okkero.skedule.schedule
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.random.Random

/**
 * Have fun.
 */
class FragAction(
    private val howMuch: Int
) : GearyAction() {
    val GearyEntity.player by get<Player>()

    override fun GearyEntity.run(): Boolean {
        player.playSound(player.location, Sound.ENTITY_GHAST_SCREAM, 1F, 0.5F)
        mineInAbyss.schedule {
            for (i in 0..howMuch) {
                if(player.isDead)
                    break;
                val creeper = (player.world.spawnEntity(
                    player.location.add(
                        Vector(
                            Random.nextDouble(-2.0, 2.0),
                            Random.nextDouble(0.0, 2.0),
                            Random.nextDouble(-2.0, 2.0),
                        )
                    ),
                    EntityType.CREEPER
                ) as Creeper)
                creeper.isPowered = true
                waitFor(2)
            }
        }
        return true
    }
}
