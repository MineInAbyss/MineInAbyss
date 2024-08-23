package com.mineinabyss.features.guidebook

import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.idofront.serialization.SerializableItemStack
import net.minecraft.core.component.DataComponentPredicate
import net.minecraft.core.component.DataComponents
import net.minecraft.util.Unit
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer

object GuideBookHelpers {

    fun MerchantOffer(buyItem: SerializableItemStack): MerchantOffer {
        return MerchantOffer(ItemStack.fromBukkitCopy(buyItem.toItemStack()))
    }

    fun MerchantOffer(buyItem: ItemStack, sellItemStack: ItemStack = ItemStack.fromBukkitCopy(TitleItem.transparentItem)): MerchantOffer {
        buyItem.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE)
        return MerchantOffer(ItemCost(buyItem.itemHolder, 1, DataComponentPredicate.allOf(buyItem.components)), sellItemStack, 1, 1, 1f)
    }
}