package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffectBuilder

/**
 * @property name the name of this Layer. This name may not match the world name.
 * @property sub The sub header.
 * @property sections The sections in this layer. This list is immutable.
 * @property ascensionEffects The effects of ascending on this layer.
 * @property maxCurseRadius The radius at which the curse has maximum effect.
 * @property minCurseRadius The radius at which the curse has minimum effect.
 * @property minCurseMultiplier Effectively multiply the distance you travel up by this amount when you are closer than
 * minCurseRadius away.
 * @property maxCurseMultiplier Effectively multiply the distance you travel up by this amount when you are further than
 * maxCurseRadius away.
 * @property curseOverrideRegions Regions that have custom curse levels.
 * @property startDepth Starting depth of this layer.
 * @property endDepth End depth of this layer.
 * @property deathMessage Custom death message suffix for this Layer.
 */
interface Layer {
    val name: String?
    val sub: String?
    val deathMessage: String? get() = " in the depths of the abyss"
    val ascensionEffects: List<AscensionEffectBuilder<*>>
    val maxCurseRadius: Float
    val minCurseRadius: Float
    val minCurseMultiplier: Float
    val maxCurseMultiplier: Float
    val curseOverrideRegions: List<CurseRegion>
    val startDepth: Int
    val endDepth: Int
    val sections: List<Section>

    /** Returns whether or not this layer contains this section */
    fun containsSection(section: Section): Boolean

}