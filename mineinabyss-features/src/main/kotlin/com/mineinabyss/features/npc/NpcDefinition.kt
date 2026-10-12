package com.mineinabyss.features.npc

import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import com.mineinabyss.idofront.serialization.VectorAltSerializer
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.util.Vector

@Serializable
class NpcDefinition(
    val name: String,
    val location: @Serializable(LocationAltSerializer::class) Location,
    val scale: @Serializable(VectorAltSerializer::class) Vector = Vector(1, 1, 1),
    val model: String? = null,
    val showName: Boolean = true,
    val speaker: String? = null,
    val dialogStyle: String? = null,
    val onInteract: Tasks? = null,
) {
    val speakerName: String
        get() = speaker ?: PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(name))
}
