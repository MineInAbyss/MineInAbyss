package com.mineinabyss.features.respawn

import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Location

/** Respawn point for players without a bonfire, active once they cross [depth] in either direction */
@Serializable
class RespawnEntry(
    val id: String,
    val depth: Int,
    @Serializable(with = LocationAltSerializer::class)
    val location: Location,
)

@Serializable
class RespawnConfig(val respawns: List<RespawnEntry> = listOf()) {
    @Transient
    val byId = respawns.associateBy { it.id }

    init {
        require(byId.size == respawns.size) { "Respawn entries must have unique ids" }
    }
}
