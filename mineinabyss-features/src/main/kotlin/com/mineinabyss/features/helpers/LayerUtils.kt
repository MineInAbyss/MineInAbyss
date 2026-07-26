package com.mineinabyss.features.helpers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.deeperworld.datastructures.Section
import com.mineinabyss.deeperworld.sections.section
import com.mineinabyss.features.abyss
import org.bukkit.Location
import org.bukkit.World


val Location.layer: Layer? get() = this.section?.let { abyss.layers.worldManager.getLayerForSection(it.key) }

val Section.layer: Layer? get() = abyss.layers.worldManager.getLayerForSection(key)

val World.isAbyssWorld: Boolean get() = abyss.layers.worldManager.isAbyssWorld(this)
