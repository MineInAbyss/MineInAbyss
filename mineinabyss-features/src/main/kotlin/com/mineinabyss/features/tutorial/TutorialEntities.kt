package com.mineinabyss.features.tutorial

import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.Vector3fSerializer
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
    val backgroundColor: @Serializable(ColorSerializer::class) Color = Color.fromARGB(0, 0, 0, 0),
    val shadow: Boolean = true,
    val alignment: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
    val billboard: Display.Billboard = Display.Billboard.VERTICAL,
    val scale: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(1f, 1f, 1f),
    val viewRange: Float? = null,
)
