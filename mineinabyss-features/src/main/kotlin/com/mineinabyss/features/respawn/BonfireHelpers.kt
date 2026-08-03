package com.mineinabyss.features.respawn
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

// Little hack i'm trying to avoid depending on the bonfire plugin, unsure if it works properly
//TODO: test
@kotlinx.serialization.Serializable
@SerialName("bonfire:bonfire_respawn")
data class BonfireRespawn(
    val bonfireUuid: @Serializable(UUIDSerializer::class) UUID,
    val bonfireLocation: @kotlinx.serialization.Serializable(LocationSerializer::class) Location,
)

fun Player.hasBonfireActive() : Boolean {
    player?.toGeary()?.get<BonfireRespawn>() ?: return false
    return true
}

fun Player.getBonfireLocation() : String {
    val respawn = player?.toGeary()?.get<BonfireRespawn>() ?: return "None"
    return respawn.bonfireLocation.toString()
}