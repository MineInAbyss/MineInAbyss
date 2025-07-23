package com.mineinabyss.components.layer

import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.components.music.Song
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.idofront.serialization.MiniMessageSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.Component
import org.bukkit.Material

/**
 * @property name the name of this Layer. This name may not match the world name.
 * @property sub The sub header.
 * @property sections The sections in this layer. This list is immutable.
 * @property ascensionEffects The effects of ascending on this layer.
 * @property startDepth Starting depth of this layer.
 * @property endDepth End depth of this layer.
 * @property deathMessage Custom death message suffix for this Layer.
 */
@Serializable
class Layer(
    val id: String,
    val name: String,
    val sub: String,
    val deathMessage: @Serializable(MiniMessageSerializer::class) Component = Component.text("in the depths of the abyss"),
    val depth: Depth = Depth(0, 0),
    @SerialName("effects")
    val ascensionEffects: List<AscensionEffect> = emptyList(),
    val hasPvpDefault: Boolean = false,
    val liquidFlowLimit: Int = -1,
    val blockBlacklist: List<Material> = emptyList(),
    @SerialName("songs")
    val _songs: List<String> = emptyList(),
    @SerialName("sections")
    val _sections: List<String> = emptyList(),
    val equipWhistleCosmetic: Boolean = false,
) {
    @Transient val songs: List<Song> = emptyList()
    @Transient val sections: List<Section> = _sections.mapNotNull { WorldManager.getSectionFor(it) }
    val startDepth: Int get() = depth.start
    val endDepth: Int get() = depth.end
    val key: LayerKey get() = LayerKey(name)

    operator fun contains(section: Section): Boolean = sections.any { it.key == section.key }
}

@Serializable
class Depth(
    val start: Int,
    val end: Int
)
