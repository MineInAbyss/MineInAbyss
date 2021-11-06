package com.mineinabyss.components.gondolas

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("mineinabyss:gondola")
@AutoscanComponent
class Gondola(
    @Serializable(with = LocationSerializer::class)
    val location: Location,
    val name: String,
    val displayItem: SerializableItemStack,
)
