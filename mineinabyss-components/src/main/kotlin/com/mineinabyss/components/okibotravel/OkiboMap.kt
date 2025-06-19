package com.mineinabyss.components.okibotravel

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector
import org.joml.Math
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

internal fun vectorFromString(vector: String, defaultValue: Float): Vector {
    val floats = vector.replace(" ", "").split(",").dropLastWhile(String::isEmpty)
        .map { it.toFloatOrNull() ?: defaultValue }.toMutableList()
    while (floats.size < 3) floats.add(defaultValue)
    return Vector(floats[0], floats[1], floats[2])
}

internal fun vector3fFromString(vector: String, defaultValue: Float): Vector3f {
    val floats = vector.replace(" ", "").split(",").dropLastWhile(String::isEmpty)
        .map { it.toFloatOrNull() ?: defaultValue }.toMutableList()
    while (floats.size < 3) floats.add(defaultValue)
    return Vector3f(floats[0], floats[1], floats[2])
}

@Serializable
@SerialName("mineinabyss:okibo_map")
data class OkiboMap(
    val station: String,
    val location: @Serializable(LocationSerializer::class) Location,
    @EncodeDefault(NEVER) @SerialName("text") private val _text: String = ":orthmap|1::space_-1::orthmap|2:<newline><newline><newline>:orthmap|3::space_-1::orthmap|4:",
    @EncodeDefault(NEVER) @SerialName("scale") private val _scale: String = "1,1,1",
    @EncodeDefault(NEVER) @SerialName("offset") private val _offset: String = "0,0,0",
    @SerialName("hitboxes") val _hitboxes: Set<String>,
    val icon: Icon? = Icon()
) {

    @Transient val hitboxes = mutableSetOf<OkiboMapHitbox>()
    @Transient val offset = vector3fFromString(_offset, 0f)
    @Transient val scale = vector3fFromString(_scale, 1f)
    @Transient val text = _text.miniMsg()

    @Serializable
    @SerialName("mineinabyss:okibo_map_hitbox")
    data class OkiboMapHitbox(
        val destStation: String,
        @SerialName("offset") private val _offset: String,
        @EncodeDefault(NEVER) val hitbox: Hitbox = Hitbox()
    ) {
        @Transient val offset = vectorFromString(_offset, 0f)

        fun offset(angle: Float): Vector {
            var angle = angle
            if (angle < 0) angle += 360f

            val radians = Math.toRadians(-angle.toDouble()) // Negate for clockwise yaw
            val x = offset.x * cos(radians) - offset.z * sin(radians)
            val z = offset.x * sin(radians) + offset.z * cos(radians)

            return Vector(x, offset.y, z)
        }
    }
    @Serializable
    data class Hitbox(val width: Double = 0.15, val height: Double = 0.15)

    @Serializable
    data class Icon(val text: String = ":orthmap_icon:", @SerialName("offset") private val _offset: String = "0,0,2", @SerialName("scale") private val _scale: String = "1,1,1") {
        @Transient val offset = vectorFromString(_offset, 0f).toVector3f().rotateY(90f)
        @Transient val scale = vectorFromString(_scale, 0f).toVector3f()

        fun offset(angle: Float): Vector3f {
            var angle = angle
            if (angle < 0) angle += 360f

            val radians = Math.toRadians(-angle.toDouble()) // Negate for clockwise yaw
            val x = offset.x * cos(radians) - offset.z * sin(radians)
            val z = offset.x * sin(radians) + offset.z * cos(radians)

            return Vector3f(x.toFloat(), offset.y, z.toFloat())
        }
    }

    companion object {
        val background = Color.fromARGB(0,0,0,0).asARGB()
    }
}
