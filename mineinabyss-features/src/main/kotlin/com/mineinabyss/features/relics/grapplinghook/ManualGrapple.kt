package com.mineinabyss.features.relics.grapplinghook

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
    }
}
