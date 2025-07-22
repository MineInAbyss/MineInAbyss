package com.mineinabyss.components.gondolas

import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("mineinabyss:gondola")
class Gondola(
    @Serializable(with = LocationSerializer::class)
    val upperLoc: Location,
    @Serializable(with = LocationSerializer::class)
    val lowerLoc: Location,
    val name: String, // the name of the gondola
    val unlockPrice: Int, // the price needed to unlock access to the line
    val displayItem: SerializableItemStack, // the item to display in the GUI
    val warpZoneRange: Double, // the range in which the player needs to stay in order to be teleported
    val noAccessMessage: String, // the message to display when the player tries to use the gondola without perms
) {
    val warpCooldown = 5000
}
