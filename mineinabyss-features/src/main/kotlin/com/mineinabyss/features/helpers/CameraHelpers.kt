package com.mineinabyss.features.helpers

import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

// F5 cannot be blocked, at zero distance both third person views render from the eye
fun LivingEntity.zeroCameraDistance() {
    if (getAttribute(Attribute.CAMERA_DISTANCE) == null) registerAttribute(Attribute.CAMERA_DISTANCE)
    getAttribute(Attribute.CAMERA_DISTANCE)?.baseValue = 0.0
}

fun Player.lockCameraDistance(key: NamespacedKey) {
    getAttribute(Attribute.CAMERA_DISTANCE)?.run {
        removeModifier(key)
        addTransientModifier(AttributeModifier(key, -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
    }
}

fun Player.releaseCameraDistance(key: NamespacedKey) {
    getAttribute(Attribute.CAMERA_DISTANCE)?.removeModifier(key)
}
