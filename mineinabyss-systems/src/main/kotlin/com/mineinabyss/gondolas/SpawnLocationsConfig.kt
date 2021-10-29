package com.mineinabyss.mineinabyss.adventure.gondolas

import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.mineinabyss.core.mineInAbyss
import kotlinx.serialization.Serializable
import org.bukkit.Location
import java.io.File

object SpawnLocationsConfig : IdofrontConfig<SpawnLocationsConfig.Data>(
    mineInAbyss,
    Data.serializer(),
    File(mineInAbyss.dataFolder, "spawn-locs.yml")
) {
    @Serializable
    class Data(
        val spawns: MutableList<SpawnLocation>
    )
}

@Serializable
data class SpawnLocation(
    @Serializable(with = LocationSerializer::class)
    val location: Location,
    val displayItem: SerializableItemStack,
    val cost: Int,
)
