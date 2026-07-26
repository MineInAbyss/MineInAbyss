package com.mineinabyss.features.layers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.components.layer.LayerConfig
import com.mineinabyss.components.layer.LayerKey
import com.mineinabyss.deeperworld.sections.SectionKey
import com.mineinabyss.deeperworld.sections.SectionRepository
import org.bukkit.World

/**
 * Abyss world manager that assumes a single abyss for the whole server.
 */
class SingleAbyssWorldManager(
    layerConfigs: List<LayerConfig>,
    sections: SectionRepository,
) : AbyssWorldManager {
    override val layers = layerConfigs.map { Layer.from(sections, it) }.associateBy { it.key }

    private val sectionToLayer: Map<SectionKey, Layer> = layers
        .values
        .flatMap { layer -> layer.sections.map { it.key to layer } }
        .toMap()

    private val abyssWorlds = layers.values
        .flatMap { it.sections }
        .map { it.world }
        .distinct()

    override fun isAbyssWorld(world: World) = world in abyssWorlds

    override fun getLayerForSection(key: SectionKey) = sectionToLayer[key]

    override fun getLayerFor(key: LayerKey) = layers[key]
}
