package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffectBuilder

class LayerImpl(
        override val name: String,
        override val sub: String,
        override val index: Int,
        override val deathMessage: String?,
        override val maxCurseRadius: Float,
        override val minCurseRadius: Float,
        override val minCurseMultiplier: Float,
        override val maxCurseMultiplier: Float,
        override val curseOverrideRegions: List<CurseRegion>,
        override val startDepth: Int,
        override val endDepth: Int
) : Layer {
    private var effects: List<AscensionEffectBuilder<*>> = mutableListOf()

    override var ascensionEffects: List<AscensionEffectBuilder<*>>
        get() = effects.toList()
        set(value) { effects = value}

    override var sections: List<Section> = listOf()
        get() = field.toList()

    override fun containsSection(section: Section): Boolean = sections.any { it.key == section.key }
}