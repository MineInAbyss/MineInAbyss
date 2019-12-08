package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffectBuilder
import java.util.*

class LayerImpl(override val name: String, override val sub: String, override val index: Int) : Layer {
    override var sections: List<Section> = listOf()
        get() = Collections.unmodifiableList(field)
    private var effects: List<AscensionEffectBuilder<*>> = mutableListOf()

    override fun containsSection(section: Section): Boolean = sections.any { it.key == section.key }

    fun setEffects(effects: List<AscensionEffectBuilder<*>>) {
        this.effects = effects
    }

    override val ascensionEffects: List<AscensionEffectBuilder<*>>
        get() = Collections.unmodifiableList(effects)

    init {
        effects = ArrayList()
    }
}