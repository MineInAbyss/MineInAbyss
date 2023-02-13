package com.mineinabyss.components.npc.shopkeeping

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
@SerialName("ShopTrade")
class ShopTrade(
    val item: SerializableItemStack,
    val currency: SerializableItemStack? = null,
    val currencyType: ShopCurrency = ShopCurrency.ORTH_COIN,
    val price: Int
) {
    operator fun component1(): SerializableItemStack {
        return item
    }
    operator fun component2(): SerializableItemStack? {
        return currency
    }
    operator fun component3(): ShopCurrency {
        return currencyType
    }
    operator fun component4(): Int {
        return price
    }
}

enum class ShopCurrency {
    ORTH_COIN, MITTY_TOKEN, ITEM
}

object ShopTradeSerializer : KSerializer<ShopTrade> {
    override val descriptor: SerialDescriptor = ShopTrade.serializer().descriptor

    override fun serialize(encoder: Encoder, value: ShopTrade) {
        encoder.encodeSerializableValue(ShopTrade.serializer(), ShopTrade(value.item, value.currency, value.currencyType, value.price))
    }

    override fun deserialize(decoder: Decoder): ShopTrade {
        val surrogate = decoder.decodeSerializableValue(ShopTrade.serializer())
        return ShopTrade(surrogate.item, surrogate.currency, surrogate.currencyType, surrogate.price)
    }
}
