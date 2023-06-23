package com.mineinabyss.components.tutorial

import com.mineinabyss.idofront.serialization.ColorSerializer
import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.TextDisplay.TextAlignment

@Serializable
data class TutorialEntity(
    val location: @Serializable(LocationSerializer::class) Location,
    val text: String,
    val backgroundColor: @Serializable(ColorSerializer::class) Color = Color.fromARGB(0, 0, 0, 0),
    val shadow: Boolean = true,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val billboard: Billboard = Billboard.VERTICAL,
)
