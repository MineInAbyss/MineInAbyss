package com.mineinabyss.components.descent

import com.mineinabyss.components.layer.LayerKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Serializable
@SerialName("mineinabyss:descent_context")
class DescentContext(
    @Serializable(with = DateAsLongSerializer::class)
    val startDate: Date = Date(),
    var expOnDescent: Double = 0.0,
) {
    val pinUsedLayers = mutableSetOf<LayerKey>()

    //TODO implement
    val lowestDepth: Int = 0

    @Transient
    var confirmedLeave: Boolean = false
}

object DateAsLongSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeLong(value.time)
    override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeLong())
}
