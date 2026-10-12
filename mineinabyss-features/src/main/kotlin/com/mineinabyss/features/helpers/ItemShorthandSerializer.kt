package com.mineinabyss.features.helpers

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import com.mineinabyss.idofront.serialization.SerializableItemStackSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias ShorthandItemStack = @Serializable(with = ItemShorthandSerializer::class) BaseSerializableItemStack

/** Idofront's item shorthand with an optional trailing amount, like `mineinabyss:orth_coin 3` */
object ItemShorthandSerializer : KSerializer<BaseSerializableItemStack> {
    private val itemStack = SerializableItemStackSerializer()
    override val descriptor = itemStack.descriptor

    override fun deserialize(decoder: Decoder): BaseSerializableItemStack {
        val shorthand = ((decoder as? YamlInput)?.node as? YamlScalar)?.content?.trim() ?: return itemStack.deserialize(decoder)
        val amount = shorthand.substringAfterLast(' ', "").toIntOrNull()
        val id = if (amount == null) shorthand else shorthand.substringBeforeLast(' ').trim()
        return itemStack.decodeFromShorthand(id).copy(amount = amount)
    }

    override fun serialize(encoder: Encoder, value: BaseSerializableItemStack) = itemStack.serialize(encoder, value)
}
