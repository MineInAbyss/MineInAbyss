package com.mineinabyss.features.locations

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.Location

/** A key from locationPoints.yml, or a location written as `x,y,z yaw,pitch world` */
@Serializable(with = LocationPointSerializer::class)
@ExcludeAutoScan
sealed interface LocationPoint {
    /** A copy, so callers may change it */
    fun resolve(): Location

    /** Null when the point is in a loaded world */
    fun problem(): String? = if (resolve().isWorldLoaded) null else "location $this is in an unloaded world"

    class Literal(val location: Location) : LocationPoint {
        override fun resolve(): Location = location.clone()
        override fun toString() = "${location.x},${location.y},${location.z}"
    }

    // Looked up on every use, so reloading locationPoints.yml reaches everything that references it
    class Reference(val key: PrefabKey) : LocationPoint {
        init {
            require(locationPoints?.contains(key) == true) { "Unknown location point $key" }
        }

        override fun resolve(): Location = checkNotNull(locationPoints?.get(key)) { "Location point $key no longer exists" }
        override fun toString() = key.full
    }
}

object LocationPointSerializer : KSerializer<LocationPoint> {
    override val descriptor = PrimitiveSerialDescriptor("mineinabyss:location_point", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocationPoint {
        val scalar = (decoder as? YamlInput)?.node as? YamlScalar
        val string = scalar?.content ?: decoder.decodeString()
        referenceKey(string)?.let { return LocationPoint.Reference(it) }

        checkLiteral(string)
        val location = if (scalar != null) LocationAltSerializer.deserialize(decoder)
        else Yaml.default.decodeFromYamlNode(LocationAltSerializer, YamlScalar(string, YamlPath.root))
        return LocationPoint.Literal(location)
    }

    override fun serialize(encoder: Encoder, value: LocationPoint) = when (value) {
        is LocationPoint.Reference -> encoder.encodeString(value.key.full)
        is LocationPoint.Literal -> LocationAltSerializer.serialize(encoder, value.location)
    }

    private fun referenceKey(string: String): PrefabKey? =
        string.trim().takeIf { ':' in it && ',' !in it }?.let(PrefabKey::ofOrNull)

    /** LocationAltSerializer never rejects input, it reads missing or broken numbers as 0 and unknown worlds as the default world, which would send players to the wrong place */
    fun checkLiteral(string: String) {
        val parts = string.split(" ")
        fun isNumbers(part: String, count: Int) = part.split(",").let { numbers -> numbers.size == count && numbers.all { it.toDoubleOrNull() != null } }
        val world = when {
            parts.size > 3 || !isNumbers(parts[0], 3) -> null
            parts.size == 3 -> parts[2].takeIf { isNumbers(parts[1], 2) }
            parts.size == 2 -> parts[1].takeUnless { isNumbers(it, 2) } ?: ""
            else -> ""
        } ?: throw SerializationException("'$string' is neither a location point key nor a location written as x,y,z [yaw,pitch] [world]")
        if (world.isNotEmpty() && Bukkit.getWorld(world) == null) throw SerializationException("Unknown world '$world' in location '$string'")
    }
}

/** Resolves a [LocationPoint] once while decoding, for things placed in the world on load that a later reload of locationPoints.yml should not move */
object ResolvedLocationPointSerializer : KSerializer<Location> {
    override val descriptor = LocationPointSerializer.descriptor

    override fun deserialize(decoder: Decoder): Location = LocationPointSerializer.deserialize(decoder).resolve()

    override fun serialize(encoder: Encoder, value: Location) = LocationAltSerializer.serialize(encoder, value)
}
