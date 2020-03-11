package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.WorldManager
import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffectBuilder
import com.derongan.minecraft.mineinabyss.ascension.effect.configuration.EffectConfiguror
import com.google.common.collect.ImmutableList
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.Configuration

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
class AbyssWorldManagerImpl(config: Configuration) : AbyssWorldManager {
    private val _layers: MutableList<Layer> = mutableListOf()
    override val layers: List<Layer>
        get() = ImmutableList.copyOf(_layers)
    private val abyssWorlds: MutableSet<World> = hashSetOf()
    private var numLayers = 0

    @Suppress("UNCHECKED_CAST")
    private fun parseLayer(map: Map<*, *>): Layer {
        val layerName = map[NAME_KEY] as String
        val subHeader = map[SUB_KEY] as String
        val layer = LayerImpl(layerName, subHeader, numLayers++, deathMessage = " ${map.getOrDefault(DEATH_MESSAGE_KEY, "in $layerName")}",
                maxCurseMultiplier = map["maxCurseMultiplier"] as Float,
                minCurseMultiplier =  map["minCurseMultiplier"] as Float,
                maxCurseRadius = map["maxCurseRadius"] as Float,
                minCurseRadius = map["minCurseRadius"] as Float,
                curseOverrideRegions = emptyList()
        )
        _layers.add(layer)

        val worldManager = Bukkit.getServicesManager().load(WorldManager::class.java)!!
        val sections = (map[SECTION_KEY] as List<String>).map { worldManager.getSectionFor(it) }
        layer.sections = sections
        sections.forEach { abyssWorlds.add(it.world) }

        val effectMap = map[EFFECTS_KEY] as List<Map<*, *>?>? ?: emptyList<Map<*, *>>()
        layer.setEffects(effectMap.mapNotNull { parseAscensionEffects(it) })
        return layer
    }

    private fun parseAscensionEffects(map: Map<*, *>?): AscensionEffectBuilder<*> =
            EffectConfiguror.createBuilderFromMap(map)

    override fun getLayerForSection(section: Section) = _layers.first { it.containsSection(section) }

    override fun isAbyssWorld(worldName: World) = abyssWorlds.contains(worldName)

    init {
        config.getMapList(LAYER_KEY).forEach { parseLayer(it) }
    }
}