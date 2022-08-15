package com.mineinabyss.npc.shopkeeping

import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("shopkeeping")
class ShopKeepingFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(ShopKeepingListener())

        commands {
            mineinabyss {
                "shop" {

                }
            }
        }
    }
}
