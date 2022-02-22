package com.mineinabyss.tools

import com.mineinabyss.components.tools.Flamethrower
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.helpers.getRightSide
import com.mineinabyss.helpers.spawnParticleAlongLine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Particle
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:shoot_flame")
class ShootFlame()

@AutoScan
class FlamethrowerListener : GearyListener() {
    private val TargetScope.player by get<Player>()
    private val SourceScope.flamethrower by get<Flamethrower>()

    init {
        event.has<ShootFlame>()
    }

    //TODO Make particles after operationsPerBlock not spawn
    //TODO Add interactions on particle-contact with player/block
    //TODO Try and make it not be a static line, but a progressive one to get an arching flame
    //TODO Make the flame-portion thicker by spawning more particles in a small Random around main point
    //TODO Fuel integration would probably be based on durability system later
    @Handler
    fun TargetScope.fireFlamethrower(source: SourceScope) {
        val end = player.eyeLocation.clone().add(player.eyeLocation.direction.normalize().multiply(source.flamethrower.flameReach))
        val start = getRightSide(player.eyeLocation, 0.5)
        spawnParticleAlongLine(start, end, Particle.FLAME, 60, 0, 0.1, 0.1, 0.1, 0.0, null, false) { l ->
            val hitPlayers = l!!.getNearbyPlayers(0.0)
            l.block.isPassable && hitPlayers.isEmpty()
        }
    }
}
