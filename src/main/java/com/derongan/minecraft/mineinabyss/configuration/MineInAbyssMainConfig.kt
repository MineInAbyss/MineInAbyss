package com.derongan.minecraft.mineinabyss.configuration

import com.charleskorn.kaml.Yaml
import com.derongan.minecraft.mineinabyss.ascension.effect.EffectRegistration
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.world.LayerImpl
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

internal object MineInAbyssMainConfig : IdofrontConfig<MineInAbyssMainConfig.Data>(
        mineInAbyss,
        Data.serializer(),
        format = Yaml(
                serializersModule = EffectRegistration.module,
        )
) {
    @Serializable
    class Storage(
            val relicpath: String
    )

    @Serializable
    class Data(
            val storage: Storage,
            val layers: List<LayerImpl>//TODO way of changing the serializer from service
    )
}