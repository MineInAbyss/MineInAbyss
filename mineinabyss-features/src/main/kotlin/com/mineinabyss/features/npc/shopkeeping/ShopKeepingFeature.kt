package com.mineinabyss.features.npc.shopkeeping

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.features.abyss
import com.mineinabyss.features.lootcrates.prefabKey
import com.mineinabyss.features.npc.DialogsConfig
import com.mineinabyss.features.npc.NpcEntity
import com.mineinabyss.features.npc.NpcManager
import com.mineinabyss.features.npc.NpcsConfig
import com.mineinabyss.features.npc.shopkeeping.menu.ShopMainMenu
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.feature
import kotlinx.coroutines.delay
import org.bukkit.Bukkit.getWorld
import org.bukkit.World
import org.koin.core.module.dsl.scopedOf
import kotlin.time.Duration.Companion.seconds


object listenerSingleton {
    var sgl: NpcsConfig? = null
    var bstgth: MutableMap<Long, List<NpcEntity>> = mutableMapOf()
}

val ShopKeepingFeature = feature("shopkeepers") {
    dependsOn {
        plugins("ModelEngine")
    }

    scopedModule {
        scoped<World> { getWorld("world")!! }
        scopedOf(::ShopKeepingListener)
    }

    onEnable {

        listeners(get<ShopKeepingListener>())

    }
    mainCommand {
        "shops" {
            executes.asPlayer().args("key" to Args.prefabKey().oneOf { ShopKeepers.getKeys() }) { shopKey ->
                player.withGeary {
                    val shopKeeper = shopKey.toEntityOrNull()?.get<ShopKeeper>() ?: return@args
                    guiy(player) { ShopMainMenu(player, shopKeeper) }
                }
            }
        }
    }
}
