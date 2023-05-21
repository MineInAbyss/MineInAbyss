package com.mineinabyss.components.cosmetics

import com.mineinabyss.idofront.serialization.ItemStackSerializer
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

@Serializable
data class BackpackStorage(val backpack: @Serializable(with = ItemStackSerializer::class) ItemStack)
