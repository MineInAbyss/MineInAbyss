package com.mineinabyss.mineinabyss.core

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.mineinabyss.components.layer.Layer
import com.mineinabyss.idofront.plugin.getService
import kotlinx.serialization.Serializable

interface MIAConfig {
    val data: Data

    companion object : MIAConfig by getService()

    /**
     * @param layers A list of all the layers and sections composing them to be registered.
     * @property hubSection The hub section of the abyss, a safe place for living and trading.
     */
    @Serializable
    class Data(
        val layers: List<Layer>, //TODO way of changing the serializer from service
        val features: List<AbyssFeature>,
        private val hubSectionName: String = "orth",
    ) {
        val hubSection by lazy {
            WorldManager.getSectionFor(hubSectionName) ?: error("Section $hubSectionName was not found for the hub.")
        }
    }
}
