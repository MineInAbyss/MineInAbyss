package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class LayerImpl(
        override val name: String,
        override val sub: String,
        override val deathMessage: String? = "in the depths of the abyss",
        override val maxCurseRadius: Float = 2000f,
        override val minCurseRadius: Float = 1000f,
        override val minCurseMultiplier: Float = 1f,
        override val maxCurseMultiplier: Float = 1f,
        override val curseOverrideRegions: List<CurseRegion> = emptyList(),
        val depth: Depth = Depth(0, 0),
        @SerialName("effects")
        override val ascensionEffects: List<AscensionEffect> = emptyList(),
        @SerialName("sections")
        val _sections: List<String> = emptyList(),
) : Layer {
    @Transient
    override val sections: List<Section> = _sections.mapNotNull { WorldManager.getSectionFor(it) }
    override val startDepth: Int get() = depth.start
    override val endDepth: Int get() = depth.end

    override fun contains(section: Section): Boolean = sections.any { it.key == section.key }
}

@Serializable
class Depth(
        val start: Int,
        val end: Int
)
