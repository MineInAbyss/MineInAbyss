package com.mineinabyss.components.okibotravel

import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Location

@Serializable
@SerialName("mineinabyss:okibo_line_station")
data class OkiboLineStation(
    val id: String,
    val displayName: String,
    val location: @Serializable(with = LocationAltSerializer::class) Location,
    @SerialName("iconHitboxOffset") private val _iconHitboxOffset: String,
) {
    /** Where this station sits on every okibo map, relative to the board with x pointing out of it */
    @Transient
    val iconHitboxOffset = vector3fFromString(_iconHitboxOffset, 0f)
}
