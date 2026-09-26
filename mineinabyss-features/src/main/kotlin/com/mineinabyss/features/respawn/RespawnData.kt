package com.mineinabyss.features.respawn

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:respawn_checkpoint")
data class RespawnCheckpoint(val id: String)
