package com.mineinabyss.components.okibotravel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("mineinabyss:okibo_traveler")
data class OkiboTraveler(val selectedDestination: OkiboLineStation)

