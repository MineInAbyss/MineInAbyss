package com.derongan.minecraft.mineinabyss.configuration

import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.Serializable
import org.bukkit.Location
import java.io.File

internal object SpawnLocationsConfig : IdofrontConfig<SpawnLocationsConfig.Data>(
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