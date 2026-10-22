package com.mineinabyss.features.helpers

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import com.mineinabyss.idofront.serialization.SerializableItemStackSerializer
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// The contextual descriptor is what makes kaml hand over scalars and maps alike
private val SCALAR_OR_MAP: SerialDescriptor = ContextualSerializer(Any::class).descriptor

private fun Decoder.isScalar() = (this as? YamlInput)?.node is YamlScalar

/**
 * Reads a scalar with [fromScalar] and anything else with [full].
 * Wrapped in a value class action, this lets `- action: value` stand in for the action's map form
 */
open class ScalarShorthandSerializer<T>(
    private val full: KSerializer<T>,
    private val fromScalar: (Decoder) -> T,
) : KSerializer<T> {
    override val descriptor = SCALAR_OR_MAP

    override fun deserialize(decoder: Decoder): T = if (decoder.isScalar()) fromScalar(decoder) else full.deserialize(decoder)

    override fun serialize(encoder: Encoder, value: T) = full.serialize(encoder, value)
}

/** A key written as a scalar, or the value itself written in place as a map */
sealed interface KeyOrInline<T> {
    fun resolveWith(lookup: (PrefabKey) -> T?): T?

    class Reference<T>(val key: PrefabKey) : KeyOrInline<T> {
        override fun resolveWith(lookup: (PrefabKey) -> T?) = lookup(key)
        override fun toString() = key.full
    }

    class Inline<T>(val value: T) : KeyOrInline<T> {
        override fun resolveWith(lookup: (PrefabKey) -> T?) = value
        override fun toString() = "inline"
    }
}

/** [inline] is only read when decoding, so a value may contain references to its own type */
open class KeyOrInlineSerializer<T>(inline: () -> KSerializer<T>) : KSerializer<KeyOrInline<T>> {
    private val inline by lazy(inline)
    override val descriptor = SCALAR_OR_MAP

    override fun deserialize(decoder: Decoder): KeyOrInline<T> =
        if (decoder.isScalar()) KeyOrInline.Reference(decoder.decodeSerializableValue(PrefabKey.serializer()))
        else KeyOrInline.Inline(inline.deserialize(decoder))

    override fun serialize(encoder: Encoder, value: KeyOrInline<T>) = when (value) {
        is KeyOrInline.Reference -> encoder.encodeSerializableValue(PrefabKey.serializer(), value.key)
        is KeyOrInline.Inline -> inline.serialize(encoder, value.value)
    }
}

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
