package com.mineinabyss.components.tutorial

import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.idofront.serialization.Vector3fSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.TextDisplay.TextAlignment
import org.joml.Vector3f

@Serializable
data class TutorialEntity(
    val location: @Serializable(LocationSerializer::class) Location,
    val text: String,
    val backgroundColor: @Serializable(ColorSerializer::class) Color = Color.fromARGB(0, 0, 0, 0),
    val shadow: Boolean = true,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val billboard: Billboard = Billboard.VERTICAL,
    val scale: @Serializable(Vector3fSerializer::class) Vector3f = Vector3f(1f, 1f, 1f),
    val viewRange: Float? = null,
)
