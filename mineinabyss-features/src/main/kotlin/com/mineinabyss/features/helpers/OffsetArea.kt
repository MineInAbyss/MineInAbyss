package com.mineinabyss.features.helpers

import com.mineinabyss.idofront.serialization.SingleOrListSerializer
import com.mineinabyss.idofront.util.DoubleRange
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Location
import kotlin.math.roundToInt

class OffsetArea(val x: DoubleRange, val y: DoubleRange, val z: DoubleRange) {
    fun blockOffsets(): Sequence<Triple<Int, Int, Int>> = sequence {
        for (bx in x.blocks()) for (by in y.blocks()) for (bz in z.blocks()) yield(Triple(bx, by, bz))
    }

    val blockCount get() = x.blocks().count() * y.blocks().count() * z.blocks().count()

    fun randomIn(location: Location): Location = location.clone().add(x.randomOrMin(), y.randomOrMin(), z.randomOrMin())

    private fun DoubleRange.blocks() = start.roundToInt()..endInclusive.roundToInt()

    companion object {
        val NONE = OffsetArea(0.0..0.0, 0.0..0.0, 0.0..0.0)
    }
}

object OffsetAreasSerializer : SingleOrListSerializer<OffsetArea>(OffsetAreaSerializer)

object OffsetAreaSerializer : KSerializer<OffsetArea> {
    override val descriptor = PrimitiveSerialDescriptor("OffsetArea", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): OffsetArea {
        val string = decoder.decodeString()
        val parts = string.split(",").map { it.trim() }
        if (parts.size != 3) throw SerializationException("Offset '$string' must be 'x,y,z' where each part is a number or min..max")
        val (x, y, z) = parts.map { parseRange(it) ?: throw SerializationException("Invalid range '$it' in offset '$string'") }
        return OffsetArea(x, y, z)
    }

    // Splitting on ".." only, so negative numbers like -5..-2 parse correctly
    private fun parseRange(string: String): DoubleRange? {
        val values = string.split("..").map { it.trim().toDoubleOrNull() ?: return null }
        return when (values.size) {
            1 -> values[0]..values[0]
            2 -> minOf(values[0], values[1])..maxOf(values[0], values[1])
            else -> null
        }
    }

    override fun serialize(encoder: Encoder, value: OffsetArea) {
        fun DoubleRange.format() = if (start == endInclusive) "$start" else "$start..$endInclusive"
        encoder.encodeString("${value.x.format()},${value.y.format()},${value.z.format()}")
    }
}
