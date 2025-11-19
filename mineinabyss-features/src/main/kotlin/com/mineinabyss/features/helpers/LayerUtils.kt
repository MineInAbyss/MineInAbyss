package com.mineinabyss.features.helpers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.features.abyss
import com.mineinabyss.features.layers.AbyssWorldManager
import com.mineinabyss.features.layers.LayersFeature
import org.bukkit.Location
import org.bukkit.World


val Location.layer: Layer? get() = this.section?.let { abyss.getScoped<AbyssWorldManager>(LayersFeature).getLayerForSection(it) }

val Section.layer: Layer? get() = abyss.getScoped<AbyssWorldManager>(LayersFeature).getLayerForSection(this)

val World.isAbyssWorld: Boolean get() = abyss.getScoped<AbyssWorldManager>(LayersFeature).isAbyssWorld(this)
