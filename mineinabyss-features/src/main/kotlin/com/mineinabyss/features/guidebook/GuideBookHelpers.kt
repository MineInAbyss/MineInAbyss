package com.mineinabyss.features.guidebook

import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.serialization.SerializableItemStack
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponentPredicate
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Unit
import net.minecraft.world.entity.npc.ClientSideMerchant
import net.minecraft.world.inventory.MerchantMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers
import org.bukkit.entity.Player

object GuideBookHelpers {
    internal val MerchantMenuTrader = MerchantMenu::class.java.getDeclaredField("trader").apply { isAccessible = true }

    fun Collection<GuideBookButton>.toMerchantOffers() =
        MerchantOffers().apply { addAll(this@toMerchantOffers.map(GuideBookButton::merchantOffer)) }

    fun MerchantOffer(buyItem: SerializableItemStack): MerchantOffer {
        return MerchantOffer(ItemStack.fromBukkitCopy(buyItem.toItemStack()))
    }

    fun MerchantOffer(buyItem: org.bukkit.inventory.ItemStack): MerchantOffer {
        return MerchantOffer(ItemStack.fromBukkitCopy(buyItem))
    }

    fun MerchantOffer(buyItem: ItemStack, sellItemStack: ItemStack = ItemStack.fromBukkitCopy(TitleItem.transparentItem)): MerchantOffer {
        sellItemStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE)
        return MerchantOffer(ItemCost(buyItem.itemHolder, 1, DataComponentPredicate.allOf(buyItem.components)), sellItemStack, 0, 1, 1f)
    }

}