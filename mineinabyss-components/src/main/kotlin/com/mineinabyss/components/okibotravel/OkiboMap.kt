package com.mineinabyss.components.okibotravel

import com.mineinabyss.idofront.serialization.Vector3fSerializer
import com.mineinabyss.idofront.serialization.VectorSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.util.Vector
import org.joml.Vector3f

@Serializable
@SerialName("mineinabyss:okibo_map")
data class OkiboMap(
    val station: String,
    @EncodeDefault(NEVER) val offset: @Serializable(VectorSerializer::class) Vector = Vector(0, 0, 0),
    @EncodeDefault(NEVER) val text: String = """
      
      
      
      
      
      
      
      
      
      
    """.trimIndent(),
    @EncodeDefault(NEVER) val font: String = "orth_map",
    @EncodeDefault(NEVER) val scale: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(1f, 1f, 1f),
    val hitboxes: Set<OkiboMapHitbox>
) {
    @Serializable
    @SerialName("mineinabyss:okibo_map_hitbox")
    data class OkiboMapHitbox(
        val destStation: String,
        val offset: @Serializable(VectorSerializer::class) Vector,
        @EncodeDefault(NEVER) val hitbox: Hitbox = Hitbox()
    )
    @Serializable
    data class Hitbox(val width: Double = 0.3, val height: Double = 0.3)
}
