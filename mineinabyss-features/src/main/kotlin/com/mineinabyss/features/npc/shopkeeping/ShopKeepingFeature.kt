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
        scoped<NpcsConfig> { config("npc", abyss.dataPath, NpcsConfig()).getOrLoad() }
        scoped<DialogsConfig> { config("dialogs", abyss.dataPath, DialogsConfig()).getOrLoad() }
        scoped<World> { getWorld("world")!! }
        scopedOf(::ShopKeepingListener)
        scopedOf(::NpcManager)
    }

    onEnable {
        val manager = get<NpcManager>()

        listeners(get<ShopKeepingListener>(), manager)

        abyss.plugin.launch {
            delay(10.seconds) //TODO correctly schedule loading when necessary
            runCatching { manager.initNpc() }.onFailure { it.printStackTrace() }
        }
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
        "npcs" {
            "test" {
                executes.asPlayer {
                    get<NpcsConfig>().npcs.values.forEach {
                        player.sendMessage("${it.displayName} - ${it.id}")
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
}
