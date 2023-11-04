package com.mineinabyss.features.advancements

import com.mineinabyss.components.advancements.CustomAdvancement
import com.mineinabyss.features.helpers.di.Features.advancements
import eu.endercentral.crazy_advancements.NameKey
import eu.endercentral.crazy_advancements.advancement.Advancement
import eu.endercentral.crazy_advancements.advancement.progress.GenericResult
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
data class AdvancementConfig(
    val advancements: Map<String, CustomAdvancement> = emptyMap(),
)
val ADVANCEMENT_NAMEKEY = NameKey("mineinabyss:advancement_manager")
fun NameKey.toAdvancement(): Advancement? = advancements.advancementManager.getAdvancement(this)
fun CustomAdvancement.toAdvancement(): Advancement? =
    NameKey(advancements.config.advancements.entries.find { it.value == this }?.key).toAdvancement()
fun getAdvancement(id: String): Advancement? = NameKey("mineinabyss", id).toAdvancement()
fun Player.grantAdvancement(id: String): Boolean = getAdvancement(id)?.let { advancements.advancementManager.grantAdvancement(this, it) == GenericResult.CHANGED } ?: false
fun Player.revokeAdvancement(id: String): Boolean = getAdvancement(id)?.let { advancements.advancementManager.revokeAdvancement(this, it) == GenericResult.CHANGED } ?: false
fun CustomAdvancement.mapChildren(): Map<String, CustomAdvancement> =
    children.toMutableMap() + children.entries.map { it.value.mapChildren() }.flatMap { it.entries }.associate { it.key to it.value }
