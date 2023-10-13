package com.mineinabyss.features.helpers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.deeperworld.world.section.Section
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.mineinabyss.core.abyss
import org.bukkit.Location
import org.bukkit.World


val Location.layer: Layer? get() = this.section?.let { Features.layers.worldManager.getLayerForSection(it) }

val Section.layer: Layer? get() = Features.layers.worldManager.getLayerForSection(this)

val World.isAbyssWorld: Boolean get() = Features.layers.worldManager.isAbyssWorld(this)
