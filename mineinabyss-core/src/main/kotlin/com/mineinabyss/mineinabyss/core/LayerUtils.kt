package com.mineinabyss.mineinabyss.core

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.deeperworld.world.section.section
import org.bukkit.Location
import org.bukkit.World


val Location.layer: Layer? get() = this.section?.let { abyss.worldManager.getLayerForSection(it) }

val Section.layer: Layer? get() = abyss.worldManager.getLayerForSection(this)

val World.isAbyssWorld: Boolean get() = abyss.worldManager.isAbyssWorld(this)
