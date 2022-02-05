package com.mineinabyss.plugin

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.components.layer.LayerKey
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.mineinabyss.core.AbyssWorldManager
import com.mineinabyss.mineinabyss.core.MIAConfig
import org.bukkit.World

/**
 * @property _layers A private mutable list of layers
 * @property layers An immutable list of layers accessible to outside classes
 * @property abyssWorlds A list of worlds that are part of the abyss
 */
class AbyssWorldManagerImpl : AbyssWorldManager {
    private var _layers: List<Layer> = MIAConfig.data.layers
    override val layers = _layers.associateBy { it.key }
    private val abyssWorlds = mutableSetOf<World>()

    override fun isAbyssWorld(world: World) = world in abyssWorlds

    override fun getLayerForSection(section: Section) = _layers.firstOrNull { section in it }

    override fun getLayerFor(key: LayerKey) = layers[key]

    init {
        //add all worlds into abyssWorlds
        _layers.flatMap { it.sections }
            .map { it.world }
            .distinct()
            .forEach { abyssWorlds.add(it) }
    }
}
