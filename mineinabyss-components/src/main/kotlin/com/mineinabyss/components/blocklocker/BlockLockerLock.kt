package com.mineinabyss.components.blocklocker

import com.mineinabyss.components.core.SignOwner
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("mineinabyss:blocklocker_lock")
data class BlockLockerLock(
    val owner: @Serializable(UUIDSerializer::class) UUID,
    val allowedPlayers: MutableSet<@Serializable(UUIDSerializer::class) UUID> = mutableSetOf(owner)
)