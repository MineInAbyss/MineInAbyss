package com.mineinabyss.features.tutorial

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.deeperworld.world.Region
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.Vector3fSerializer
import com.mineinabyss.idofront.textcomponents.miniMsg
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.coroutines.delay
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

val tutorial by DI.observe<TutorialContext>()
interface TutorialContext {
    val firstJoinLocation: Location?
    val tutorialEntities: Long2ObjectOpenHashMap<ObjectArrayList<TutorialEntity>>
    val entry: TutorialRegion
    val exit: TutorialRegion
}

@Serializable
data class Tutorial(
    @EncodeDefault(NEVER) val firstJoinLocation: @Serializable(LocationSerializer::class) Location? = null,
    val tutorialEntities: List<TutorialEntity> = listOf(),
    val start: TutorialRegion = TutorialRegion(),
    val end: TutorialRegion = TutorialRegion(),
)

@Serializable
data class TutorialRegion(
    val region: Region = Region(0,0,0,0,0,0),
    @SerialName("target") private val _target: String = "0,0,0",
    private val targetYaw: Float = 0f,
) {

    @Transient val target = _target.toLocation(Bukkit.getWorlds().first()).apply { yaw = targetYaw }

    private fun String.toLocation(world: World): Location {
        val (x,y,z) = this.replace(" ", "").split(",", limit = 3).map { it.toDoubleOrNull() ?: 0.0 }
        return Location(world, x, y, z)
    }
}

@Serializable
@SerialName("mineinabyss:tutorial_entity")
data class TutorialEntity(
    val location: @Serializable(LocationSerializer::class) Location,
    val text: String,
    @EncodeDefault(NEVER) val textOpacity: Byte = (-1).toByte(),
    @EncodeDefault(NEVER) val lineWidth: Int = 200,
    @EncodeDefault(NEVER) val backgroundColor: @Serializable(ColorSerializer::class) Color = Color.fromARGB(0, 0, 0, 0),
    @EncodeDefault(NEVER) val shadow: Boolean = true,
    @EncodeDefault(NEVER) val alignment: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
    @EncodeDefault(NEVER) val billboard: Display.Billboard = Display.Billboard.VERTICAL,
    @EncodeDefault(NEVER) val scale: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(1f, 1f, 1f),
    @EncodeDefault(NEVER) val leftRotation: @Serializable(QuaternionfSerializer::class) Quaternionf = Quaternionf(),
    @EncodeDefault(NEVER) val rightRotation: @Serializable(QuaternionfSerializer::class) Quaternionf = Quaternionf(),
    @EncodeDefault(NEVER) val seeThrough: Boolean = false,
) {
    private fun TextDisplay.trackEntity(tutorial: TutorialEntity) {
        toGearyOrNull()?.setPersisting(tutorial) ?: run {
            abyss.plugin.launch {
                delay(10.ticks)
                trackEntity(tutorial)
            }
        }
    }

    fun spawn() {
        if (location.isChunkLoaded) location.world.spawn(location, TextDisplay::class.java) { textDisplay ->
            textDisplay.text(text.miniMsg())
            textDisplay.billboard = billboard
            textDisplay.textOpacity = textOpacity
            textDisplay.lineWidth = lineWidth
            textDisplay.alignment = alignment
            textDisplay.isShadowed = shadow
            textDisplay.backgroundColor = backgroundColor
            textDisplay.transformation = Transformation(textDisplay.transformation.translation, leftRotation, scale, rightRotation)
            textDisplay.isSeeThrough = seeThrough

            textDisplay.isPersistent = false
        }.trackEntity(this)
    }
}
