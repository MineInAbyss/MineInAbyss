package com.mineinabyss.features.layers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.components.layer.LayerKey
import com.mineinabyss.deeperworld.world.section.Section
import org.bukkit.World

/**
 * Abyss world manager that assumes a single abyss for the whole server.
 */
class SingleAbyssWorldManager(
    config: LayersConfig,
) : AbyssWorldManager {
    override val layers = config.layers.associateBy { it.key }

    private val sectionToLayer: Map<Section, Layer> =
        layers.flatMap { layer -> layer.value.sections.map { it to layer.value } }.toMap()

    private val abyssWorlds = layers.flatMap { it.value.sections }.map { it.world }.distinct()

    override fun isAbyssWorld(world: World) = world in abyssWorlds

    override fun getLayerForSection(section: Section) = sectionToLayer[section]

    override fun getLayerFor(key: LayerKey) = layers[key]
}
