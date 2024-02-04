package com.mineinabyss.features.tutorial

import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.MiniMessageSerializer
import com.mineinabyss.idofront.serialization.Vector3fSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
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
    @EncodeDefault(NEVER) val viewRange: Float? = null,
)
