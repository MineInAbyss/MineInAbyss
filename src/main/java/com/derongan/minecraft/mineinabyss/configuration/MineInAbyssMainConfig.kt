package com.derongan.minecraft.mineinabyss.configuration

import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.world.Layer
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.serialization.Serializable

internal object MineInAbyssMainConfig : IdofrontConfig<MineInAbyssMainConfig.Data>(mineInAbyss, Data.serializer()) {
    @Serializable
    class Storage(
            val relicpath: String
    )

    @Serializable
    class Data(
            val storage: Storage,
            val layers: List<Layer>
    )
}