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
    val price: Int,
) {
    operator fun component1(): SerializableItemStack {
        return item
    }
    operator fun component2(): Int {
        return price
    }
}

object ShopTradeSerializer : KSerializer<ShopTrade> {
    override val descriptor: SerialDescriptor = ShopTrade.serializer().descriptor

    override fun serialize(encoder: Encoder, value: ShopTrade) {
        encoder.encodeSerializableValue(ShopTrade.serializer(), ShopTrade(value.item, value.price))
    }

    override fun deserialize(decoder: Decoder): ShopTrade {
        val surrogate = decoder.decodeSerializableValue(ShopTrade.serializer())
        return ShopTrade(surrogate.item, surrogate.price)
    }
}
