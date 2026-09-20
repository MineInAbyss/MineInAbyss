package com.mineinabyss.components.okibotravel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/** A destination a player picked on an okibo map, waiting for them to click it again to confirm */
@Serializable
@SerialName("mineinabyss:okibo_traveler")
data class OkiboTraveler(val destinationId: String, val selectedAt: Long = System.currentTimeMillis()) {
    fun isValid(timeout: Duration) = System.currentTimeMillis() - selectedAt <= timeout.inWholeMilliseconds
}
