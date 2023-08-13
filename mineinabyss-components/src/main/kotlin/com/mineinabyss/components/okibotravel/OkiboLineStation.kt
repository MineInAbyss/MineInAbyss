package com.mineinabyss.components.okibotravel

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("mineinabyss:okibo_line_station")
data class OkiboLineStation(
    val name: String,
    val location: @Serializable(with = LocationSerializer::class) Location,
    //val subStations: Set<OkiboLineStation> = setOf()
)
