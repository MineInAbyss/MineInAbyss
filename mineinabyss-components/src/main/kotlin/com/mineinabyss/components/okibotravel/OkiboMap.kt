package com.mineinabyss.components.okibotravel

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Color
import org.bukkit.util.Vector
import org.joml.Vector3f

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
    val noticeBoardFurniture: NoticeBoardFurniture? = NoticeBoardFurniture(),
    @EncodeDefault(NEVER) @SerialName("offset") private val _offset: String = "0,0,0",
    @EncodeDefault(NEVER) val yaw: Float = 0f,
    @EncodeDefault(NEVER) @SerialName("text") private val _text: String = ":orthmap|1::space_-1::orthmap|2:<newline><newline><newline>:orthmap|3::space_-1::orthmap|4:",
    @EncodeDefault(NEVER) @SerialName("scale") private val _scale: String = "1,1,1",
    val hitboxes: Set<OkiboMapHitbox>,
    val icon: Icon? = Icon()
) {

    @Transient val offset = vectorFromString(_offset, 0f)
    @Transient val scale = vector3fFromString(_scale, 1f)
    @Transient val text = _text.miniMsg()
    @Transient val background = Color.fromARGB(0,0,0,0).asARGB()

    @Serializable
    data class NoticeBoardFurniture(
        private val prefab: String = "mineinabyss:noticeboard_okibo",
        @EncodeDefault(NEVER) @SerialName("offset") private val _offset: String = "0,0,0",
        val yaw: Float = 0f,
    ) {
        @Transient val prefabKey = PrefabKey.of(prefab)
        @Transient val offset = vectorFromString(_offset, 0f)
    }
    @Serializable
    @SerialName("mineinabyss:okibo_map_hitbox")
    data class OkiboMapHitbox(
        val destStation: String,
        @SerialName("offset") private val _offset: String,
        @EncodeDefault(NEVER) val hitbox: Hitbox = Hitbox()
    ) {
        @Transient val offset = vectorFromString(_offset, 0f)
    }
    @Serializable
    data class Hitbox(val width: Double = 0.3, val height: Double = 0.3)

    @Serializable
    data class Icon(val text: String = ":orthmap_icon:", @SerialName("offset") private val _offset: String = "0,0,2") {
        @Transient val offset = vectorFromString(_offset, 0f)
    }
}
