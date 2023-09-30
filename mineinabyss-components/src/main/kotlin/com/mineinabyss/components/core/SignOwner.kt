package com.mineinabyss.components.core

import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("mineinabyss:sign_owner")
data class SignOwner(val owner: @Serializable(UUIDSerializer::class) UUID)
