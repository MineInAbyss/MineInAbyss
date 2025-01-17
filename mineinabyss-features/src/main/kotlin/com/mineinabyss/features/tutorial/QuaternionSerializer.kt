package com.mineinabyss.features.tutorial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joml.Quaternionf

@Serializable
@SerialName("Quaternion")
private class QuaternionSurrogate(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val w: Float = 0f,
)

object QuaternionfSerializer : KSerializer<Quaternionf> {
    override val descriptor: SerialDescriptor = QuaternionSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Quaternionf) {
        encoder.encodeSerializableValue(QuaternionSurrogate.serializer(), QuaternionSurrogate(value.x, value.y, value.z, value.w))
    }

    override fun deserialize(decoder: Decoder): Quaternionf {
        val surrogate = decoder.decodeSerializableValue(QuaternionSurrogate.serializer())
        return Quaternionf(surrogate.x, surrogate.y, surrogate.z, surrogate.w)
    }
}
