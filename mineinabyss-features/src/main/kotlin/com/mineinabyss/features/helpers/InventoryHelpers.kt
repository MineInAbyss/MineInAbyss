package com.mineinabyss.features.helpers

import com.mineinabyss.idofront.nms.aliases.toNMS
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun Player.similarItems(item: ItemStack?): List<ItemStack> = inventory.contents.filterNotNull().filter { it.isSimilar(item) }
fun Player.similarItemAmount(item: ItemStack?) = similarItems(item).sumOf { it.amount }
fun Player.adjustItemStackAmountFromCost(item: ItemStack?, amount: Int): ItemStack? {
    var remaining = amount
    if (item == null || similarItemAmount(item) < amount) return null
    similarItems(item).forEach { stack ->
        val stackAmount = stack.amount
        if (stackAmount > remaining) return stack.subtract(remaining)
        else {
            remaining -= stackAmount
            stack.subtract(stackAmount)
        }
    }
    return null
}

fun Player.sendTransparentSlots(slots: IntRange) = toNMS().run {
    val empty = CraftItemStack.asNMSCopy(TitleItem.transparentItem)
    val stateId = inventoryMenu.incrementStateId()
    slots.forEach { connection.send(ClientboundContainerSetSlotPacket(inventoryMenu.containerId, stateId, it, empty)) }
}
