package com.mineinabyss.features.npc

import com.mineinabyss.features.helpers.ShorthandItemStack
import com.mineinabyss.features.helpers.loading.KeyedRegistry
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.MerchantRecipe

@Serializable
class Trade(
    val input: ShorthandItemStack,
    val secondInput: ShorthandItemStack? = null,
    val output: ShorthandItemStack,
)

@Serializable
class TradeTable(val title: String? = null, val trades: List<Trade> = emptyList()) {
    private fun recipes(): List<MerchantRecipe> = trades.mapNotNull { it.toRecipe() }

    fun itemErrors(): String? {
        val failed = trades.withIndex().filter { it.value.toRecipe() == null }
        return if (failed.isEmpty()) null else "has items that could not be created in trades ${failed.joinToString { "#${it.index + 1}" }}"
    }

    private fun Trade.toRecipe(): MerchantRecipe? {
        val inputs = listOfNotNull(input, secondInput).map { it.toItemStackOrNull() ?: return null }
        val result = output.toItemStackOrNull() ?: return null

        return MerchantRecipe(result, Int.MAX_VALUE).apply { inputs.forEach(::addIngredient) }
    }

    fun open(player: Player) {
        val merchant = Bukkit.createMerchant().apply { recipes = recipes() }
        MenuType.MERCHANT.builder()
            .merchant(merchant)
            .apply { title?.let { title(it.miniMsg()) } }
            .build(player)
            .open()
    }
}

typealias TradeTables = KeyedRegistry<TradeTable>
