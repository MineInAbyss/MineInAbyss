package com.mineinabyss.components.tools.grappling

import com.mineinabyss.idofront.nms.aliases.toNMS
import kotlinx.coroutines.Job
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
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

    private val leashPacket = ClientboundSetEntityLinkPacket(bat.toNMS(), hook.toNMS())
    fun sendGrappleLeash() = Bukkit.getOnlinePlayers().forEach { (it as CraftPlayer).handle.connection.connection.send(leashPacket) }

    fun isBeneathHook() = abs(hook.location.x - player.location.x) < 1 && abs(hook.location.z - player.location.z) < 1 && hook.location.y > player.eyeLocation.y
    fun isOverHook() = hook.location.y < player.eyeLocation.y
}
