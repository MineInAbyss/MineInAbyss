package com.mineinabyss.components.moderation

import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.serialization.ItemStackSerializer
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

@Serializable
@SerialName("mineinabyss:helper_info")
data class HelperInfo(
    //TODO Replace with LocationSerializer when Idofront is updated, currently World is not serialized
    val oldLocation: OldLocSerializer,
    val oldGameMode: GameMode = GameMode.SURVIVAL,
    val inventory: List<@Serializable(with = ItemStackSerializer::class) ItemStack>,
)

@Serializable
data class OldLocSerializer(
    val world: @Serializable(with = UUIDSerializer::class) UUID,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
) {
    fun toLocation(): Location {
        return Location(Bukkit.getWorld(world), x,y,z, yaw, pitch)
    }
}

val Player.helperMode get() = this.toGeary().get<HelperInfo>()
val Player.isInHelperMode get() = this.toGeary().has<HelperInfo>()
