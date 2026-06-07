package com.mineinabyss.components.layer

import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.components.music.Song
import com.mineinabyss.deeperworld.datastructures.Section
import com.mineinabyss.deeperworld.sections.SectionRepository
import net.kyori.adventure.text.Component
import org.bukkit.Material

data class Layer(
    val id: String,
    val name: String,
    val subtitle: Component,
    val deathMessage: Component,
    val depth: Depth = Depth(0, 0),
    val ascensionEffects: List<AscensionEffect> = emptyList(),
    val hasPvpDefault: Boolean = false,
    val liquidFlowLimit: Int = -1,
    val blockBlacklist: List<Material> = emptyList(),
    val songs: List<Song> = emptyList(),
    val sections: List<Section> = emptyList(),
    val equipWhistleCosmetic: Boolean = false,
) {

    companion object {
        fun from(sections: SectionRepository, config: LayerConfig) = Layer(
            id = config.id,
            name = config.name,
            subtitle = config.sub,
            deathMessage = config.deathMessage,
            depth = config.depth,
            ascensionEffects = config.ascensionEffects,
            hasPvpDefault = config.hasPvpDefault,
            liquidFlowLimit = config.liquidFlowLimit,
            blockBlacklist = config.blockBlacklist,
            songs = listOf(), //TODO config.songs,
            sections = config.sections.mapNotNull { sections[it] },
            equipWhistleCosmetic = config.equipWhistleCosmetic,
        )
    }

    val startDepth: Int get() = depth.start
    val endDepth: Int get() = depth.end
    val key: LayerKey get() = LayerKey(name)

    operator fun contains(section: Section): Boolean = sections.any { it.key == section.key }
}