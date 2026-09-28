package com.mineinabyss.features.misc

import com.charleskorn.kaml.YamlComment
import com.destroystokyo.paper.MaterialSetTag
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.features.listeners
import kotlinx.serialization.Serializable
import org.bukkit.Material
import kotlin.random.Random

@Serializable
class MiscConfig(
    val enabled: Boolean = false,
    val grassBonemeal: GrassBonemealConfig = GrassBonemealConfig(),
)

@Serializable
class GrassBonemealConfig(
    @YamlComment("Chance for each plant bonemeal grows on grass to be swapped for one of the blocks below")
    val replaceChance: Double = 0.7,
    @YamlComment("Geary prefabs with a nexo:custom_block component, mapped to their weight")
    val prefabs: Map<PrefabKey, Double> = emptyMap(),
    @YamlComment("Vanilla block data like minecraft:poppy, mapped to their weight")
    val vanilla: Map<String, Double> = defaultFlowers(),
) {
    /** A [PrefabKey] from [prefabs] or a block data string from [vanilla], weighted across both */
    fun randomBlock(): Any? {
        val entries = prefabs.entries + vanilla.entries
        val total = entries.sumOf { it.value }
        if (total <= 0.0) return null
        val roll = Random.nextDouble(total)
        var sum = 0.0
        return entries.firstOrNull { sum += it.value; roll < sum }?.key
    }

    private companion object {
        private val rareFlowers = setOf(Material.SPORE_BLOSSOM, Material.WITHER_ROSE)

        fun defaultFlowers() = MaterialSetTag.FLOWERS.values
            .filter { it != Material.MANGROVE_PROPAGULE }
            .associate { it.key.asString() to if (it in rareFlowers) 0.07 else 1.0 }
    }
}

val MiscFeature = module("miscellaneous") {
    val config = get<AbyssFeatureConfig>().misc
    require(config.enabled) { "Misc feature is disabled" }

    listeners(MiscListener(config))
}
