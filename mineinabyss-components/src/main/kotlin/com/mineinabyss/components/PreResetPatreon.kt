package com.mineinabyss.components

import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PreResetPatreon(val name: String, val uuid: @Serializable(UUIDSerializer::class) UUID, val heldTokens: Int, val wasActivePatreon: Boolean)
