package com.mineinabyss.features.tools.grapplinghook

import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.util.*

var UUID.isGrappling: Boolean
    get() = ManualGrapple.isGrappling[this] ?: false
    set(value) {
        ManualGrapple.isGrappling[this] = value
    }
var Player.isGrappling: Boolean
    get() = ManualGrapple.isGrappling[uniqueId] ?: false
    set(value) {
        ManualGrapple.isGrappling[uniqueId] = value
    }
object ManualGrapple {
    val isGrappling = mutableMapOf<UUID, Boolean>()

    fun stopManualGrapple(player: Player) {
        player.isGrappling = false
        if (player.gameMode == GameMode.CREATIVE) {
            player.allowFlight = true
            player.isFlying = player.velocity.y !in -0.8..-0.7
        } else {
            player.allowFlight = false
            player.isFlying = false
        }
    }
}
