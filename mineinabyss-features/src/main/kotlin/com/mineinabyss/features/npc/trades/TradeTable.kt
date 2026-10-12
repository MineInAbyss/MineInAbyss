package com.mineinabyss.features.npc.trades

import com.mineinabyss.features.helpers.ShorthandItemStack
import com.mineinabyss.features.helpers.loading.KeyedYamlLoader
import com.mineinabyss.features.helpers.loading.gearyYaml
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.MerchantRecipe
import java.nio.file.Path

@Serializable
class Trade(
    val input: ShorthandItemStack,
    val secondInput: ShorthandItemStack? = null,
    val output: ShorthandItemStack,
)

@Serializable
class TradeTable(val title: String? = null, val trades: List<Trade> = emptyList()) {
    fun recipes(): List<MerchantRecipe> = trades.mapNotNull { it.toRecipe() }

    fun itemErrors(): String? {
        val failed = trades.withIndex().filter { it.value.toRecipe() == null }
        return if (failed.isEmpty()) null else "has items that could not be created in trades ${failed.joinToString { "#${it.index + 1}" }}"
    }

    private fun Trade.toRecipe(): MerchantRecipe? {
        val ingredients = listOfNotNull(input, secondInput)
        val inputs = ingredients.mapNotNull { it.toItemStackOrNull() }.takeIf { it.size == ingredients.size } ?: return null
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

class TradeTables(private val root: Path) {
    private val yaml = gearyYaml()

    var tables: Map<PrefabKey, TradeTable> = emptyMap()
        private set

    operator fun get(key: PrefabKey) = tables[key]

    fun reload(): List<String> {
        val files = KeyedYamlLoader.load(root)
        tables = files.decodeAll("trade table", TradeTable.serializer(), yaml)
        tables.forEach { (key, table) -> table.itemErrors()?.let { files.errors += "Trade table $key $it" } }
        return files.logErrors(tables.size, "trade tables")
    }
}
