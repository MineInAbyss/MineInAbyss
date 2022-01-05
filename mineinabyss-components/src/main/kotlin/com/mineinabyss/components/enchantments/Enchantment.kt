package com.mineinabyss.components.enchantments

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.enchantments.Enchantment

@Serializable
@SerialName("mineinabyss:enchantment")
class Enchantment (
    val enchantment: MutableSet<@Contextual Enchantment>? = null,
    val level: Int = 1
)