package com.mineinabyss.components.okibotravel

import com.mineinabyss.idofront.serialization.LocationAltSerializer
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Location
import org.joml.Vector3f

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
    val location: @Serializable(LocationAltSerializer::class) Location,
    @EncodeDefault(NEVER) @SerialName("text") private val _text: String = ":orthmap|1::space_-1::orthmap|2:<newline><newline><newline>:orthmap|3::space_-1::orthmap|4:",
    @EncodeDefault(NEVER) @SerialName("scale") private val _scale: String = "1,1,1",
    @EncodeDefault(NEVER) @SerialName("offset") private val _offset: String = "0,0,0",
    /** Ids of the stations to show a clickable dot for, any station left out is hidden on this board */
    @SerialName("hitboxes") val destinations: List<String> = listOf(),
    val icon: Icon? = Icon(),
) {
    @Transient val offset = vector3fFromString(_offset, 0f)
    @Transient val scale = vector3fFromString(_scale, 1f)
    @Transient val text = _text.miniMsg()

    @Serializable
    data class Icon(
        @SerialName("text") private val _text: String = ":okibo_icon:",
        /** Nudge from the station's dot, in the same frame as [OkiboLineStation.iconHitboxOffset] */
        @SerialName("offset") private val _offset: String = "0,0,0",
        @SerialName("scale") private val _scale: String = "1,1,1",
    ) {
        @Transient val text = _text.miniMsg()
        @Transient val offset = vector3fFromString(_offset, 0f)
        @Transient val scale = vector3fFromString(_scale, 1f)
    }
}
