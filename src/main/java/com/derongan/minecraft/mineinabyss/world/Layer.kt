package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.mineinabyss.ascension.effect.AscensionEffectBuilder

interface Layer {
    /**
     * Gets the name of this Layer. This name may not match the worldname.
     * @return The name of the layer
     */
    val name: String?

    /**
     * Get the sub header
     */
    val sub: String?

    /**
     * Gets the index of this Layer. Higher layers have lower index
     * @return The index of this layer
     */
    val index: Int

    /**
     * Gets the sections in this layer. This list is immutable.
     * @return An immutable list containing the sections of this layer
     */
    val sections: List<Section?>?

    /**
     * Returns whether or not this layer contains this section
     */
    fun containsSection(section: Section): Boolean

    /**
     * Gets the effects of ascending on this layer
     */
    val ascensionEffects: List<AscensionEffectBuilder<*>>

    /**
     * the radius at which the curse has maxium effect
     */
    val maxCurseRadius: Float

    /**
     * the radius at which curse has min effect
     */
    val minCurseRadius: Float


    /**
     * effectively multiply the distance you travel up by this amount when you are closer then minCurseRadius away.
     */
    val minCurseMultiplier : Float

    /**
     * effectively multiply the distance you travel up by this amount when you are further then maxCurseRadius away.
     */
    val maxCurseMultiplier : Float

    /**
     * regions that have custom curse levels
     */
    val curseOverrideRegions : List<CurseRegion>


    /**
     * Get custom death message suffix for this layer
     * @return The custom death suffix to use
     */
    val deathMessage: String?
        get() = " in the depths of the abyss"
}