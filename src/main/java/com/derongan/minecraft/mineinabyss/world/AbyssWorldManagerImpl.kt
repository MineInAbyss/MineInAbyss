package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.WorldManager
import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffectBuilder
import com.derongan.minecraft.mineinabyss.ascension.effect.configuration.EffectConfiguror
import com.google.common.collect.ImmutableList
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.configuration.Configuration

class AbyssWorldManagerImpl(config: Configuration) : AbyssWorldManager {
    private val _layers: MutableList<Layer> = mutableListOf()
    override val layers: MutableList<Layer>
        get() = ImmutableList.copyOf(_layers)
    private val abyssWorlds: MutableSet<World> = hashSetOf()
    private var numLayers = 0

    private fun parseLayer(map: Map<*, *>): Layer {
        val layerName = map[NAME_KEY] as String
        val subHeader = map[SUB_KEY] as String
        val layer = LayerImpl(layerName, subHeader, numLayers++)
        _layers.add(layer)

        val worldManager = Bukkit.getServicesManager().load(WorldManager::class.java)!!
        val sections = (map[SECTION_KEY] as List<String>).map { worldManager.getSectionFor(it) }
        layer.sections = sections
        sections.forEach { abyssWorlds.add(it.world) }

        val effectMap = map[EFFECTS_KEY] as List<Map<*, *>?>? ?: emptyList<Map<*, *>>()
        layer.setEffects(effectMap.mapNotNull { parseAscensionEffects(it) })
        return layer
    }

    private fun parseAscensionEffects(map: Map<*, *>?): AscensionEffectBuilder<*> {
        return EffectConfiguror.createBuilderFromMap(map)
    }

    override fun getLayerForSection(section: Section): Layer {
        return _layers.first { it.containsSection(section) }
    }

    override fun isAbyssWorld(worldName: World): Boolean {
        return abyssWorlds.contains(worldName)
    }

    companion object {
        private const val LAYER_KEY = "layers"
        private const val NAME_KEY = "name"
        private const val SUB_KEY = "sub"
        private const val SECTION_KEY = "sections"
        private const val EFFECTS_KEY = "effects"
    }

    init {
        val layerlist = config.getMapList(LAYER_KEY)
        layerlist.forEach { parseLayer(it) }
    }
}