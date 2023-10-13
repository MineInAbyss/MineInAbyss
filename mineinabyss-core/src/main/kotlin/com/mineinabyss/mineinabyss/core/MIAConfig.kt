package com.mineinabyss.mineinabyss.core

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.deeperworld.services.WorldManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import kotlin.reflect.full.createInstance

/**
 * @param layers A list of all the layers and sections composing them to be registered.
 * @property hubSection The hub section of the abyss, a safe place for living and trading.
 * @property guilds Guild related options.
 */
@Serializable
class AbyssConfig(
    @SerialName("features")
    private val _features: List<AbyssFeature>,
) {
    private val reflections
        get() = Reflections(
            ConfigurationBuilder()
                .forPackage("com.mineinabyss", AbyssFeature::class.java.classLoader)
                .filterInputsBy(FilterBuilder().includePackage("com.mineinabyss"))
                .addClassLoaders(AbyssFeature::class.java.classLoader)
        )

    @Transient
    val classToFeatureMap = _features
        .groupBy { it::class }
        .mapValuesTo(mutableMapOf()) { it.value.single() }
        .also { map ->
            if (map.containsKey(AllFeatures::class)) reflections
                .getSubTypesOf(AbyssFeature::class.java)
                .mapNotNull { it.kotlin.objectInstance ?: runCatching { it.kotlin.createInstance() }.getOrNull() }
                .forEach { feature ->
                    map[feature::class] = feature
                }
        }

    val features: List<AbyssFeature> get() = classToFeatureMap.values.toList()

}
