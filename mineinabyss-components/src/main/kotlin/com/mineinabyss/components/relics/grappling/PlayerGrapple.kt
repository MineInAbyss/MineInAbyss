package com.mineinabyss.components.relics.grappling

import kotlinx.coroutines.Job
import org.bukkit.Location
import org.bukkit.entity.Arrow
import org.bukkit.entity.Bat
import org.bukkit.entity.Player
import java.util.*

val hookMap = mutableMapOf<UUID, PlayerGrapple>()
data class PlayerGrapple(
    val hook: Arrow,
    val hookData: GrapplingHook,
    val player: Player,
    val pb: Bat,
    var job: Job? = null
) {
    val batAddY = 0.9

    fun terminateGrapple() {
        if (pb.isLeashed) pb.setLeashHolder(null)
        pb.remove()

        when {
            !hook.isDead -> {
                hook.remove()
                val newY = player.location.clone().apply { y += batAddY }
                if (hook.isInBlock && isProx(newY, hook.location, false) && newY.direction.y > 0.2)
                    player.velocity = player.velocity.setY(0.5 * 1.0)
            }
        }
        hookMap.remove(player.uniqueId)
    }

    fun isProx(one: Location, two: Location, ignoreY: Boolean) = isProx(one, two, ignoreY, 1.5)
    fun isProx(one: Location, two: Location, ignoreY: Boolean, nu: Double) =
        one.clone().apply { if (ignoreY) y = 0.0 }.distance(two.clone().apply { if (ignoreY) y = 0.0 }) < nu

    fun moveBatToPlayer() = pb.teleport(player.location.clone().apply { y += batAddY })
}
