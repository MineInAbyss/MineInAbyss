package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.mineinabyss.configuration.MIAConfig
import com.derongan.minecraft.mineinabyss.services.AbyssWorldManager
import org.bukkit.World

private const val LAYER_KEY = "layers"
private const val NAME_KEY = "name"
private const val SUB_KEY = "sub"
private const val SECTION_KEY = "sections"
private const val EFFECTS_KEY = "effects"
private const val DEATH_MESSAGE_KEY = "death-message"

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
