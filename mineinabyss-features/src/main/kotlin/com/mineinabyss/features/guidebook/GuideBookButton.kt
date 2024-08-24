package com.mineinabyss.features.guidebook

import com.mineinabyss.idofront.serialization.SerializableItemStack
import net.minecraft.world.item.trading.MerchantOffer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.MerchantView

data class GuideBookButton(val buttonItem: ItemStack, val action: (MerchantView) -> Unit) {

    fun plus(vararg button: GuideBookButton): List<GuideBookButton> = mutableListOf(this).plus(button)
    fun plus(buttons: Collection<GuideBookButton>): List<GuideBookButton> = mutableListOf(this).plus(buttons)

    val merchantOffer: MerchantOffer = GuideBookHelpers.MerchantOffer(buttonItem)
}