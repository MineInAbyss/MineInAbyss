package com.mineinabyss.features.guidebook

import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.serialization.SerializableItemStack
import net.minecraft.core.NonNullList
import net.minecraft.core.component.DataComponentPredicate
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Unit
import net.minecraft.world.inventory.MerchantMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player

object GuideBookHelpers {
    internal val MerchantMenuTrader = MerchantMenu::class.java.getDeclaredField("trader").apply { isAccessible = true }

    fun Collection<GuideBookButton>.toMerchantOffers() =
        MerchantOffers().apply { addAll(this@toMerchantOffers.map(GuideBookButton::merchantOffer)) }

    fun MerchantOffer(buyItem: SerializableItemStack): MerchantOffer {
        return MerchantOffer(CraftItemStack.asNMSCopy(buyItem.toItemStack()))
    }

    fun MerchantOffer(buyItem: org.bukkit.inventory.ItemStack): MerchantOffer {
        return MerchantOffer(CraftItemStack.asNMSCopy(buyItem))
    }

    fun MerchantOffer(buyItem: ItemStack, sellItemStack: ItemStack = ItemStack.fromBukkitCopy(TitleItem.transparentItem)): MerchantOffer {
        sellItemStack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE)
        return MerchantOffer(ItemCost(buyItem.itemHolder, 1, DataComponentPredicate.allOf(buyItem.components)), sellItemStack, 0, 1, 1f)
    }

    fun hideInventory(player: Player) {
        val serverPlayer = player.toNMS() as ServerPlayer
        serverPlayer.connection.send(ClientboundContainerSetContentPacket(serverPlayer.inventoryMenu.containerId,
            serverPlayer.inventoryMenu.incrementStateId(),
            NonNullList.withSize(serverPlayer.inventory.items.size, ItemStack.EMPTY), ItemStack.EMPTY
        ))
    }

    fun showInventory(player: Player) {
        val serverPlayer = player.toNMS() as ServerPlayer
        serverPlayer.connection.send(ClientboundContainerSetContentPacket(serverPlayer.inventoryMenu.containerId,
            serverPlayer.inventoryMenu.incrementStateId(),
            serverPlayer.inventory.items, ItemStack.EMPTY
        ))
    }

}