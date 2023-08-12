package com.mineinabyss.components.relics.grappling

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.mineinabyss.protocolburrito.dsl.sendTo
import kotlinx.coroutines.Job
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Arrow
import org.bukkit.entity.Bat
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs

val hookMap = mutableMapOf<UUID, PlayerGrapple>()
data class PlayerGrapple(
    val hook: Arrow,
    val hookData: GrapplingHook,
    val player: Player,
    val bat: Bat,
    var job: Job? = null
) {
    val batAddY = 1.6

    fun removeGrapple() {
        job?.cancel()
        bat.remove()

        if (!hook.isDead) {
            hook.remove()
            val newY = player.location.clone().apply { y += batAddY }
            if (hook.isInBlock && isProx(newY, hook.location, false) && newY.direction.y > 0.2)
                player.velocity = player.velocity.setY(0.5)
        }
        hookMap.remove(player.uniqueId)
    }

    fun isProx(one: Location, two: Location, ignoreY: Boolean) = isProx(one, two, ignoreY, 1.5)
    fun isProx(one: Location, two: Location, ignoreY: Boolean, nu: Double) =
        one.clone().apply { if (ignoreY) y = 0.0 }.distance(two.clone().apply { if (ignoreY) y = 0.0 }) < nu

    private val leashPacket = PacketContainer(PacketType.Play.Server.ATTACH_ENTITY).apply {
        integers.write(0, bat.entityId).write(1, hook.entityId)
    }
    fun sendGrappleLeash() = Bukkit.getOnlinePlayers().forEach { leashPacket.sendTo(it) }

    fun isBeneathHook() = abs(hook.location.x - player.location.x) < 1 && abs(hook.location.z - player.location.z) < 1 && hook.location.y > player.eyeLocation.y
    fun isOverHook() = hook.location.y < player.eyeLocation.y
}
