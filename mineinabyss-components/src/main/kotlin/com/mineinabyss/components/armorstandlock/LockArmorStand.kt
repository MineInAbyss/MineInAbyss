package com.mineinabyss.components.armorstandlock

import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("mineinabyss:lockable")
class LockArmorStand(
    val owner: @Serializable(with = UUIDSerializer::class) UUID,
    var allowedAccess: List<@Serializable(with = UUIDSerializer::class) UUID?> = listOf(owner)
)