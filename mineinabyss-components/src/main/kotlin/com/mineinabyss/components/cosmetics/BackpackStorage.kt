package com.mineinabyss.components.cosmetics

import com.mineinabyss.idofront.serialization.ItemStackSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("cosmetics:equipped_backpack_storage")
data class BackpackStorage(val backpack: @Serializable(with = ItemStackSerializer::class) ItemStack)
