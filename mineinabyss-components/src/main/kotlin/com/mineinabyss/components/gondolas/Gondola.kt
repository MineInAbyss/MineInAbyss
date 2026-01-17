package com.mineinabyss.components.gondolas


import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.Component
import org.bukkit.Location
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
@SerialName("mineinabyss:gondola")
class Gondola(
    @Serializable(with = LocationAltSerializer::class) val upperLoc: Location,
    @Serializable(with = LocationAltSerializer::class) val lowerLoc: Location,
    val displayItem: SerializableItemStack, // the item to display in the GUI
    val itemName: String,
    val warpZoneRange: Double, // the range in which the player needs to stay in order to be teleported
    val noAccessMessage: String, // the message to display when the player tries to use the gondola without perms
    val warpCooldown: @Serializable(DurationSerializer::class) Duration = 5.seconds,
    val consumeTicket: Boolean = false,
)
