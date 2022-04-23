package com.mineinabyss.mineinabyss.core

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.idofront.autoscan.AutoScanner
import com.mineinabyss.idofront.plugin.getService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.full.createInstance

interface MIAConfig {
    val data: Data

    companion object : MIAConfig by getService()

    /**
     * @param layers A list of all the layers and sections composing them to be registered.
     * @property hubSection The hub section of the abyss, a safe place for living and trading.
     * @property guilds Guild related options.
     */
    @Serializable
    class Data(
        val layers: List<Layer>, //TODO way of changing the serializer from service
        @SerialName("features")
        private val _features: List<AbyssFeature>,
        private val hubSectionName: String = "orth",
    ) {
        @Transient
        val features = (if (_features.any { it::class == AllFeatures::class }) {
            AutoScanner(this::class.java.classLoader).getSubclassesOf<AbyssFeature>()
                .mapNotNull { it.objectInstance ?: runCatching { it.createInstance() }.getOrNull() }
        } else listOf()) + _features
        val hubSection by lazy {
            WorldManager.getSectionFor(hubSectionName) ?: error("Section $hubSectionName was not found for the hub.")
        }
    }
}
