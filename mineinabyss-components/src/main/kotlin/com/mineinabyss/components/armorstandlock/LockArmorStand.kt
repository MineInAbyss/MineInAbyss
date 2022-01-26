package com.mineinabyss.components.armorstandlock

import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("mineinabyss:lockable")
data class LockArmorStand(
    val owner: @Serializable(with = UUIDSerializer::class) UUID,
    var lockState: Boolean,
    val allowedAccess: MutableSet<@Serializable(with = UUIDSerializer::class) UUID?>
) {
    fun isAllowed(uuid: UUID) : Boolean {
        return uuid in allowedAccess
    }
}