package com.mineinabyss.components.okibotravel

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("mineinabyss:okibo_line_station")
data class OkiboLineStation(
    val id: String,
    val displayName: String,
    val location: @Serializable(with = LocationSerializer::class) Location,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val subStations: Set<OkiboLineStation> = setOf(),
    @SerialName("iconHitboxOffset") val iconHitboxOffset: String
)
