package com.mineinabyss.features.helpers

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.idofront.textcomponents.toPlainText
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Serializable
class Cost(
    val orthCoins: Int = 0,
    val items: List<ShorthandItemStack> = emptyList(),
) {
    private fun itemStacks(): List<ItemStack>? = items.map { it.toItemStackOrNull() ?: return null }

    fun canAfford(player: Player): Boolean {
        if (orthCoins > 0 && (player.playerDataOrNull?.orthCoinsHeld ?: 0) < orthCoins) return false
        val stacks = itemStacks() ?: return false
        return stacks.all { stack -> player.similarItemAmount(stack) >= stacks.filter { it.isSimilar(stack) }.sumOf { it.amount } }
    }

    fun take(player: Player): Boolean {
        if (!canAfford(player)) return false
        if (orthCoins > 0) player.editPlayerData { orthCoinsHeld -= orthCoins }

        itemStacks()?.forEach { player.removeSimilar(it, it.amount) }
        return true
    }

    fun describe(): String {
        val parts = (itemStacks() ?: return "unavailable").map { "${it.amount}x ${it.effectiveName().toPlainText()}" }
        val coins = if (orthCoins > 0) listOf("$orthCoins Orth Coins") else emptyList()

        return (parts + coins).joinToString(", ").ifEmpty { "free" }
    }
}

private fun Player.similarItems(item: ItemStack?): List<ItemStack> = inventory.contents.filterNotNull().filter { it.isSimilar(item) }

fun Player.similarItemAmount(item: ItemStack?) = similarItems(item).sumOf { it.amount }

/** Does nothing unless the player holds at least [amount] items similar to [item] */
fun Player.removeSimilar(item: ItemStack?, amount: Int) {
    if (item == null || similarItemAmount(item) < amount) return
    var remaining = amount
    for (stack in similarItems(item)) {
        val taken = minOf(stack.amount, remaining)
        stack.subtract(taken)
        remaining -= taken
        if (remaining == 0) return
    }
}
