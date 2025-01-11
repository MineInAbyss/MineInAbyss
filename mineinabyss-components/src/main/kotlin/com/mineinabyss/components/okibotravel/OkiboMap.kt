package com.mineinabyss.components.okibotravel

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.Vector3fSerializer
import com.mineinabyss.idofront.serialization.VectorSerializer
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Color
import org.bukkit.util.Vector
import org.joml.Vector3f

@Serializable
@SerialName("mineinabyss:okibo_map")
data class OkiboMap(
    val station: String,
    val noticeBoardFurniture: NoticeBoardFurniture? = NoticeBoardFurniture(),
    @EncodeDefault(NEVER) val offset: @Serializable(VectorSerializer::class) Vector = Vector(0, 0, 0),
    @EncodeDefault(NEVER) val yaw: Float = 0f,
    @EncodeDefault(NEVER) @SerialName("text") private val _text: String = ":orthmap|1::space_-1::orthmap|2:<newline><newline><newline>:orthmap|3::space_-1::orthmap|4:",
    @EncodeDefault(NEVER) val scale: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(1f, 1f, 1f),
    val hitboxes: Set<OkiboMapHitbox>,
    val icon: Icon? = Icon()
) {

    @Transient val text = _text.miniMsg()
    @Transient val background = Color.fromARGB(0,0,0,0).asARGB()

    @Serializable
    data class NoticeBoardFurniture(
        private val prefab: String = "mineinabyss:noticeboard_okibo",
        val offset: @Serializable(VectorSerializer::class) Vector = Vector(0, 0, 0),
        val yaw: Float = 0f,
    ) {
        @Transient val prefabKey = PrefabKey.of(prefab)
    }
    @Serializable
    @SerialName("mineinabyss:okibo_map_hitbox")
    data class OkiboMapHitbox(
        val destStation: String,
        val offset: @Serializable(VectorSerializer::class) Vector,
        @EncodeDefault(NEVER) val hitbox: Hitbox = Hitbox()
    )
    @Serializable
    data class Hitbox(val width: Double = 0.3, val height: Double = 0.3)

    @Serializable
    data class Icon(val text: String = ":orthmap_icon:", val offset: @Serializable(VectorSerializer::class) Vector = Vector(0.0, 0.0, 0.2))
}
