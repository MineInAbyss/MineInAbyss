package com.mineinabyss.features.npc.shopkeeping

import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.helpers.prefabKeyIn
import com.mineinabyss.features.npc.shopkeeping.menu.ShopMainMenu
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.*

val ShopKeepingFeature = module("shopkeeping") {
    require(get<AbyssFeatureConfig>().shopkeeping.enabled) { "Shopkeeping feature is disabled" }

    listeners(ShopKeepingListener())
}.mainCommand {
    "shops" {
        executes.asPlayer().args("key" to Args.prefabKeyIn { ShopKeepers.getKeys() }) { shopKey ->
            player.withGeary {
                val shopKeeper = shopKey.toEntityOrNull()?.get<ShopKeeper>() ?: return@args
                guiy(player) { ShopMainMenu(player, shopKeeper) }
            }
        }
    }
}
