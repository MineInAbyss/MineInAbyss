package com.mineinabyss.features.tutorial

import kotlinx.serialization.EncodeDefault
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
    @EncodeDefault(EncodeDefault.Mode.NEVER) val x: Float = 0f,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val y: Float = 0f,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val z: Float = 0f,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val w: Float = 0f,
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
