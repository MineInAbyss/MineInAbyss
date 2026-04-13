package com.mineinabyss.features.npc.shopkeeping

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.lootcrates.prefabKey
import com.mineinabyss.features.npc.NpcManager
import com.mineinabyss.features.npc.NpcsConfig
import com.mineinabyss.features.npc.action.DialogsConfig
import com.mineinabyss.features.npc.shopkeeping.menu.ShopMainMenu
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.*
import kotlinx.coroutines.delay
import org.bukkit.Bukkit.getWorld
import kotlin.time.Duration.Companion.seconds

val ShopKeepingFeature = module("shopkeeping") {
    require(get<AbyssFeatureConfig>().shopkeeping.enabled) { "Shopkeeping feature is disabled" }
    requirePlugins("ModelEngine")

    val npcs by singleConfig<NpcsConfig>("npc.yml")
    val trades by singleConfig<TradeTablesConfig>("trades.yml")
    val dialogs by singleConfig<DialogsConfig>("dialogs.yml")
    val manager by single { NpcManager(npcs, getWorld("world")!!, dialogs) }

    TradeConfigHolder.config = trades
    listeners(ShopKeepingListener())
    plugin.launch {
        delay(10.seconds)
        runCatching { manager.initNpc() }.onFailure { it.printStackTrace() }
    }
    listeners(manager)
}.mainCommand {
    "shops" {
        executes.asPlayer().args("key" to Args.prefabKey().oneOf { ShopKeepers.getKeys() }) { shopKey ->
            player.withGeary {
                val shopKeeper = shopKey.toEntityOrNull()?.get<ShopKeeper>() ?: return@args
                guiy(player) { ShopMainMenu(player, shopKeeper) }
            }
        }
    }
    "npcs" {
        "test" {
            executes.asPlayer {
                get<NpcsConfig>().npcs.values.forEach {
                    player.sendMessage("${it.customName} - ${it.id}")
                }
            }
        }
        "reload" {
            executes {
                get<NpcManager>().initNpc()
            }
        }
    }
}
