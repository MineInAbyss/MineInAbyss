package com.mineinabyss.mineinabyss.core

import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.command.CommandSender
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import kotlin.reflect.full.createInstance

val abyssFeatures by DI.observe<AbyssFeatureConfig>()

/**
 * @param layers A list of all the layers and sections composing them to be registered.
 * @property hubSection The hub section of the abyss, a safe place for living and trading.
 * @property guilds Guild related options.
 */
@Serializable
class AbyssFeatureConfig(
    @SerialName("features")
    private val _features: List<AbyssFeature> = listOf(AllFeatures()),
) {
    private val reflections
        get() = Reflections(
            ConfigurationBuilder()
                .forPackage("com.mineinabyss", AbyssFeature::class.java.classLoader)
                .filterInputsBy(FilterBuilder().includePackage("com.mineinabyss"))
                .addClassLoaders(AbyssFeature::class.java.classLoader)
        )

    @Transient
    val features = _features
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
        .values
        .toList()

    fun reloadFeature(simpleClassName: String, sender: CommandSender) {
        val feature = features
            .find { it::class.simpleName == simpleClassName }
            ?: error("Feature not found $simpleClassName")

        with(feature) {
            runCatching { abyss.plugin.disableFeature() }
                .onSuccess { sender.success("$simpleClassName: Disabled") }
                .onFailure { sender.error("$simpleClassName: Failed to disable, $it") }
            if (feature is AbyssFeatureWithContext<*>)
                runCatching { feature.createAndInjectContext() }
                    .onSuccess { sender.success("$simpleClassName: Recreated context") }
                    .onFailure { sender.error("$simpleClassName: Failed to recreate context, $it") }
            runCatching { abyss.plugin.enableFeature() }
                .onSuccess { sender.success("$simpleClassName: Enabled") }
                .onFailure { sender.error("$simpleClassName: Failed to enable, $it") }
        }
    }
}
