package com.mineinabyss.components.playerprofile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:player_profile")
data class PlayerProfile(val background: String = "orth_background", val displayProfileArmor: Boolean = true)