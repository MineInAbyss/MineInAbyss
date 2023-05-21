package com.mineinabyss.components.npc.shopkeeping

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.entity.Player

@Serializable
@SerialName("ShopTrade")
class ShopTrade(
    val item: SerializableItemStack,
    val currency: SerializableItemStack? = null,
    val currencyType: ShopCurrency = ShopCurrency.ORTH_COIN,
    val price: Int,
    val tradeAction: @Serializable(ShopTradeActionSerializer::class) ShopTradeAction = ShopTradeAction(TradeAction.GIVE_ITEM)
) {
    operator fun component1() = item
    operator fun component2() = currency
    operator fun component3() = currencyType
    operator fun component4() = price
    operator fun component5() = tradeAction
}

enum class ShopCurrency {
    ORTH_COIN, MITTY_TOKEN, ITEM
}

@Serializable
data class ShopTradeAction(val action: TradeAction, val value: String = "") {
    fun getValue(player: Player? = null): String = (player?.let { value.replace("<player>", player.name) } ?: value)
}

enum class TradeAction {
    GIVE_ITEM, PLAYER_COMMAND, CONSOLE_COMMAND, GRANT_PERMISSION
}

object ShopTradeSerializer : KSerializer<ShopTrade> {
    override val descriptor: SerialDescriptor = ShopTrade.serializer().descriptor

    override fun serialize(encoder: Encoder, value: ShopTrade) {
        encoder.encodeSerializableValue(ShopTrade.serializer(), ShopTrade(value.item, value.currency, value.currencyType, value.price, value.tradeAction))
    }

    override fun deserialize(decoder: Decoder): ShopTrade {
        val surrogate = decoder.decodeSerializableValue(ShopTrade.serializer())
        return ShopTrade(surrogate.item, surrogate.currency, surrogate.currencyType, surrogate.price, surrogate.tradeAction)
    }
}

object ShopTradeActionSerializer : KSerializer<ShopTradeAction> {
    override val descriptor: SerialDescriptor = ShopTrade.serializer().descriptor

    override fun serialize(encoder: Encoder, value: ShopTradeAction) {
        encoder.encodeSerializableValue(ShopTradeAction.serializer(), ShopTradeAction(value.action, value.value))
    }

    override fun deserialize(decoder: Decoder): ShopTradeAction {
        val surrogate = decoder.decodeSerializableValue(ShopTradeAction.serializer())
        return ShopTradeAction(surrogate.action, surrogate.value)
    }
}
