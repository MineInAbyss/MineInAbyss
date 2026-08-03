package com.mineinabyss.features.respawn

import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Location
import java.util.UUID


@Serializable
class RespawnEntry(
    val depth: Int,
    @Serializable(with = LocationAltSerializer::class)
    val location: Location,
)

@Serializable
class RespawnConfig(
    val respawns: List<RespawnEntry>
) {
}