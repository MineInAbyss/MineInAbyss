package com.mineinabyss.features.okibotravel

import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent

private val CAMERA_KEY = NamespacedKey.fromString("mineinabyss:okibo_camera")!!

class OkiboCameraListener(private val camera: OkiboTravelConfig.CameraConfig) : Listener {

    @EventHandler(ignoreCancelled = true)
    fun PlayerItemHeldEvent.onScroll() {
        if (camera.maxDistance < 0 || !player.isRidingOkibo()) return
        val attribute = player.getAttribute(Attribute.CAMERA_DISTANCE) ?: return
        isCancelled = true

        val notches = (newSlot - previousSlot).mod(9).let { if (it > 4) it - 9 else it }
        val current = attribute.getModifier(CAMERA_KEY)?.amount ?: 0.0
        val base = attribute.value - current
        val amount = (current + notches * camera.distanceStep).coerceIn(0.0, (camera.maxDistance - base).coerceAtLeast(0.0))

        attribute.removeModifier(CAMERA_KEY)
        if (amount > 0) attribute.addTransientModifier(AttributeModifier(CAMERA_KEY, amount, AttributeModifier.Operation.ADD_NUMBER))
        player.sendActionBar(Component.text("Camera distance: %.1f".format(base + amount)))
    }

    @EventHandler
    fun EntityDismountEvent.onDismount() {
        (entity as? Player)?.resetOkiboCamera()
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.resetOkiboCamera()
    }

    @EventHandler
    fun PlayerRespawnEvent.onRespawn() {
        player.resetOkiboCamera()
    }
}

fun Player.resetOkiboCamera() {
    getAttribute(Attribute.CAMERA_DISTANCE)?.removeModifier(CAMERA_KEY)
}

private fun Player.isRidingOkibo(): Boolean {
    return MinecartMemberStore.getFromEntity(vehicle ?: return false)?.group?.properties?.matchTag(OKIBO_TAG) == true
}
