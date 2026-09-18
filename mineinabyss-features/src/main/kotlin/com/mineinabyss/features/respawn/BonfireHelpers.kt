package com.mineinabyss.features.respawn

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.serialization.helpers.componentId
import org.bukkit.Bukkit
import org.bukkit.entity.Player

// Resolved by serial name instead of by class so Bonfire stays an optional plugin rather than a classpath requirement
private const val BONFIRE_RESPAWN = "bonfire:bonfire_respawn"

private val bonfireLoaded by lazy { Bukkit.getPluginManager().isPluginEnabled("Bonfire") }

// Geary runs a single global engine, so an id resolved once stays valid for every world
private var bonfireRespawnId: ComponentId? = null

/** Whether the player picked a bonfire to respawn at, always false while Bonfire is not installed */
fun Player.hasBonfireRespawn(): Boolean {
    if (!bonfireLoaded) return false
    val geary = toGearyOrNull() ?: return false
    val id = bonfireRespawnId
        ?: runCatching { geary.world.componentId(BONFIRE_RESPAWN) }.getOrNull()?.also { bonfireRespawnId = it }
        ?: return false
    return geary.has(id)
}
