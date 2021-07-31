package com.derongan.minecraft.mineinabyss.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Material

object BlockTypeSerializer : KSerializer<Material>{
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("blockType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Material {
        return Material.valueOf(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Material) {
        encoder.encodeString(value.name)
    }
}