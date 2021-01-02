package com.derongan.minecraft.mineinabyss.configuration

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.mineinabyss.ascension.effect.EffectRegistration
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.world.LayerImpl
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

internal object MIAConfig : IdofrontConfig<MIAConfig.Data>(
    mineInAbyss, Data.serializer(),
    format = Yaml(
        serializersModule = EffectRegistration.module,
        configuration = YamlConfiguration(
            extensionDefinitionPrefix = "x-"
        )
    )
) {
    @Serializable
    class Storage(
        val relicpath: String
    )

    /**
     * @param layers A list of all the layers and sections composing them to be registered.
     * @property hubSection The hub section of the abyss, a safe place for living and trading.
     */
    @Serializable
    class Data(
        val storage: Storage,
        val layers: List<LayerImpl>, //TODO way of changing the serializer from service
        private val hubSectionName: String = "orth",
    ) {
        val hubSection by lazy { WorldManager.getSectionFor(hubSectionName) }
    }
}
