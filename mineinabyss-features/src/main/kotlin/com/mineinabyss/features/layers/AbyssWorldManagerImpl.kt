package com.mineinabyss.features.layers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.components.layer.LayerKey
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.mineinabyss.core.AbyssWorldManager
import org.bukkit.World

class AbyssWorldManagerImpl(
    layers: Collection<Layer>
) : AbyssWorldManager {
    override val layers = layers.associateBy { it.key }

    private val sectionToLayer: Map<Section, Layer> = layers
        .flatMap { layer -> layer.sections.map { it to layer } }
        .toMap()

    private val abyssWorlds = layers.flatMap { it.sections }
        .map { it.world }
        .distinct()

    override fun isAbyssWorld(world: World) = world in abyssWorlds

    override fun getLayerForSection(section: Section) = sectionToLayer[section]

    override fun getLayerFor(key: LayerKey) = layers[key]
}
