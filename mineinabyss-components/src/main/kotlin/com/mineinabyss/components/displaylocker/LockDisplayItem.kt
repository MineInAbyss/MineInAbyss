package com.mineinabyss.components.displaylocker

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*

@Serializable
@SerialName("mineinabyss:lockable")
data class LockDisplayItem(
    val owner: @Serializable(with = UUIDSerializer::class) UUID,
    var lockState: Boolean,
    val allowedAccess: MutableSet<@Serializable(with = UUIDSerializer::class) UUID>
) {
    private val bypassPermission: String = "mineinabyss.lockdisplay.bypass"
    fun hasAccess(player: Player) = !lockState || (player.uniqueId == owner || player.uniqueId in allowedAccess || player.hasPermission(bypassPermission))
}

val Entity.lockedDisplay get() = toGearyOrNull()?.get<LockDisplayItem>()
