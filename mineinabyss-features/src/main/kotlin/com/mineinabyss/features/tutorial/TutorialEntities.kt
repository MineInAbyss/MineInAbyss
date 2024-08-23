package com.mineinabyss.features.tutorial

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.Vector3fSerializer
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.coroutines.delay
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.joml.Vector3f

val tutorial by DI.observe<TutorialContext>()
interface TutorialContext {
    val tutorialEntities: List<TutorialEntity>
}

@Serializable
@SerialName("mineinabyss:tutorial_entity")
data class TutorialEntity(
    val location: @Serializable(LocationSerializer::class) Location,
    val text: String,
    @EncodeDefault(NEVER) val backgroundColor: @Serializable(ColorSerializer::class) Color = Color.fromARGB(0, 0, 0, 0),
    @EncodeDefault(NEVER) val shadow: Boolean = true,
    @EncodeDefault(NEVER) val alignment: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
    @EncodeDefault(NEVER) val billboard: Display.Billboard = Display.Billboard.VERTICAL,
    @EncodeDefault(NEVER) val scale: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(1f, 1f, 1f),
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
        location.world.spawn(location, TextDisplay::class.java) { textDisplay ->
            textDisplay.text(text.miniMsg())
            textDisplay.billboard = billboard
            textDisplay.alignment = alignment
            textDisplay.isShadowed = shadow
            textDisplay.backgroundColor = backgroundColor
            textDisplay.transformation = textDisplay.transformation.apply { scale.set(scale) }
            textDisplay.isSeeThrough = seeThrough

            textDisplay.isPersistent = false
        }.trackEntity(this)
    }
}
