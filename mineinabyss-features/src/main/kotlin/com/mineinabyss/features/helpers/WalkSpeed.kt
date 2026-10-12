package com.mineinabyss.features.helpers

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

private const val DEFAULT_WALK_SPEED = 0.2f
private val WALK_SPEED_KEY = NamespacedKey("mineinabyss", "frozen_walk_speed")

// The speed is saved with the player, so restoring it on join undoes a freeze cut off by a crash
fun Player.freezeWalkSpeed() {
    val data = persistentDataContainer
    if (!data.has(WALK_SPEED_KEY)) data.set(WALK_SPEED_KEY, PersistentDataType.FLOAT, walkSpeed.takeIf { it > 0f } ?: DEFAULT_WALK_SPEED)
    walkSpeed = 0f
}

fun Player.restoreWalkSpeed() {
    val data = persistentDataContainer
    walkSpeed = data.get(WALK_SPEED_KEY, PersistentDataType.FLOAT) ?: walkSpeed.takeIf { it > 0f } ?: DEFAULT_WALK_SPEED
    data.remove(WALK_SPEED_KEY)
}
