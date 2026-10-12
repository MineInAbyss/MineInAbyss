package com.mineinabyss.features.warps

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import com.mineinabyss.idofront.serialization.SingleOrListSerializer
import com.mineinabyss.idofront.util.DoubleRange
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Location
import kotlin.math.roundToInt

/**
 * Written as just a location like `x,y,z yaw,pitch world`,
 * or as a map with `location` and an `offset` area players land at random inside of
 */
@Serializable
@ExcludeAutoScan
class WarpDestination(
    val location: @Serializable(LocationAltSerializer::class) Location,
    val offset: @Serializable(OffsetAreaSerializer::class) OffsetArea = OffsetArea.NONE,
) {
    fun randomLocation(): Location = location.clone().add(offset.x.randomOrMin(), offset.y.randomOrMin(), offset.z.randomOrMin())
}

object WarpDestinationShorthand : KSerializer<WarpDestination> {
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

    override fun deserialize(decoder: Decoder): WarpDestination {
        if ((decoder as? YamlInput)?.node is YamlScalar) return WarpDestination(LocationAltSerializer.deserialize(decoder))
        return WarpDestination.serializer().deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: WarpDestination) = WarpDestination.serializer().serialize(encoder, value)
}

class OffsetArea(val x: DoubleRange, val y: DoubleRange, val z: DoubleRange) {
    fun blockOffsets(): Sequence<Triple<Int, Int, Int>> = sequence {
        for (bx in x.blocks()) for (by in y.blocks()) for (bz in z.blocks()) yield(Triple(bx, by, bz))
    }

    val blockCount get() = x.blocks().count() * y.blocks().count() * z.blocks().count()

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
