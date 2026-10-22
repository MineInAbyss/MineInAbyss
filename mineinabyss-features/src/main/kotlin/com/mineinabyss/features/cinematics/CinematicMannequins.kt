package com.mineinabyss.features.cinematics

import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.zeroCameraDistance
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Location
import org.bukkit.entity.Mannequin
import org.bukkit.inventory.EquipmentSlot

private const val MAX_TURN_PER_TICK = 10f
private const val MAX_PITCH_PER_TICK = 4f
private val COPIED_SLOTS = listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)

internal fun CinematicSession.spawnMannequin(step: MannequinStep) {
    remove(step.id)
    val location = step.at?.let(anchor::at) ?: player.location
    val mannequin = player.world.spawn(location, Mannequin::class.java) { mannequin ->
        mannequin.isImmovable = true
        mannequin.setGravity(false)
        mannequin.isInvulnerable = true
        mannequin.isSilent = true
        mannequin.isPersistent = false
        mannequin.isVisibleByDefault = false
        mannequin.description = null
        mannequin.zeroCameraDistance()
        if (step.copyPlayer) {
            mannequin.profile = ResolvableProfile.resolvableProfile(player.playerProfile)
            mannequin.mainHand = player.mainHand
            COPIED_SLOTS.forEach { slot -> mannequin.equipment.setItem(slot, player.equipment.getItem(slot)) }
        }
    }
    player.showEntity(abyss, mannequin)
    mannequins[step.id] = mannequin
}

internal suspend fun CinematicSession.walk(step: WalkStep) {
    val mannequin = mannequins[step.id] ?: return abyss.logger.w("Cinematic $key walks unknown mannequin ${step.id}")
    if (step.id in riders) return abyss.logger.w("Cinematic $key walks mannequin ${step.id} while it rides a gondola")
    val points = listOf(mannequin.location) + step.path.map(anchor::at)
    val lengths = points.zipWithNext { a, b -> a.distance(b) }
    val total = lengths.sum()
    val perTick = step.speed / TICKS_PER_SECOND
    var yaw = mannequin.location.yaw
    var pitch = mannequin.location.pitch
    var travelled = 0.0
    while (travelled < total && mannequin.isValid) {
        travelled = if (skipping) total else (travelled + perTick).coerceAtMost(total)
        var remaining = travelled
        var segment = 0
        while (segment < lengths.lastIndex && remaining > lengths[segment]) remaining -= lengths[segment++]
        val from = points[segment]
        val direction = points[segment + 1].toVector().subtract(from.toVector())
        val position = from.clone().add(direction.clone().multiply(if (lengths[segment] > 0) remaining / lengths[segment] else 1.0))
        if (direction.x != 0.0 || direction.z != 0.0) yaw = turn(yaw, position.clone().setDirection(direction).yaw)
        pitch -= pitch.coerceIn(-MAX_PITCH_PER_TICK, MAX_PITCH_PER_TICK)
        val next = (if (step.snapToGround) ground(position) else position).also { it.yaw = yaw; it.pitch = pitch }
        mannequin.teleport(next)
        mannequin.bodyYaw = yaw
        tick()
    }
    if (step.path.lastOrNull()?.yaw != null) {
        val target = points.last().yaw
        while (mannequin.isValid && Location.normalizeYaw(target - yaw) != 0f) {
            yaw = if (skipping) target else turn(yaw, target)
            mannequin.setRotation(yaw, 0f)
            mannequin.bodyYaw = yaw
            tick()
        }
    }
}

private fun turn(from: Float, to: Float) = from + Location.normalizeYaw(to - from).coerceIn(-MAX_TURN_PER_TICK, MAX_TURN_PER_TICK)
