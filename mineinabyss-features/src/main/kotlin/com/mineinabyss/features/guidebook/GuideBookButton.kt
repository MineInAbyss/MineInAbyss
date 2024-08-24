package com.mineinabyss.features.guidebook

import com.mineinabyss.idofront.serialization.SerializableItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.MerchantView

data class GuideBookButton(val buttonItem: ItemStack, val action: (MerchantView) -> Unit)