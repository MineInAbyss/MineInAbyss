package com.mineinabyss.components.gondolas


import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.Component
import org.bukkit.Location

@Serializable
@SerialName("mineinabyss:gondola")
class Gondola(
    @Serializable(with = LocationSerializer::class)
    val upperLoc: Location,
    @Serializable(with = LocationSerializer::class)
    val lowerLoc: Location,
    val displayItem: SerializableItemStack, // the item to display in the GUI
    val displayName: String,
    val warpZoneRange: Double, // the range in which the player needs to stay in order to be teleported
    @SerialName("no_access_message")
    private val _noAccessMessage: String, // the message to display when the player tries to use the gondola without perms
    val warpCooldown: Long = 5000
) {
    @Transient
    val parsedNoAccessMessage: Component get() = _noAccessMessage.miniMsg()
    @Transient
    val rawNoAccessMessage: String get() = _noAccessMessage
}
