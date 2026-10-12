package com.mineinabyss.features.helpers

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.playerDataOrNull
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/** Orth coins come from the bank, items from the player's inventory */
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

        itemStacks()?.forEach { player.adjustItemStackAmountFromCost(it, it.amount) }
        return true
    }

    fun refund(player: Player) {
        if (orthCoins > 0) player.editPlayerData { orthCoinsHeld += orthCoins }
        itemStacks()?.forEach { stack ->
            player.inventory.addItem(stack).values.forEach { player.world.dropItem(player.location, it) }
        }
    }

    /** Amounts of the same item are added together, an item without an amount counts as one */
    operator fun plus(other: Cost): Cost {
        val merged = (items + other.items)
            .groupBy { it.copy(amount = null) }
            .map { (item, stacks) -> item.copy(amount = stacks.sumOf { it.amount ?: 1 }) }

        return Cost(orthCoins + other.orthCoins, merged)
    }

    fun describe(): String {
        val parts = (itemStacks() ?: return "unavailable").map { "${it.amount}x ${PlainTextComponentSerializer.plainText().serialize(it.effectiveName())}" }
        val coins = if (orthCoins > 0) listOf("$orthCoins Orth Coins") else emptyList()

        return (parts + coins).joinToString(", ").ifEmpty { "free" }
    }

    companion object {
        val FREE = Cost()
    }
}
