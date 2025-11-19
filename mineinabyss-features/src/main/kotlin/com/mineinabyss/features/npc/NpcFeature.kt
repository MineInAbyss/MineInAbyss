package com.mineinabyss.features.npc

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.features.abyss
import com.mineinabyss.features.lootcrates.prefabKey
import com.mineinabyss.features.npc.shopkeeping.ShopKeepers
import com.mineinabyss.features.npc.shopkeeping.ShopKeepingListener
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

val NpcFeature = feature("npc") {
    dependsOn {
        plugins("LuxDialogues")
    }

    scopedModule {
        scoped<NpcsConfig> { config("npc", abyss.dataPath, NpcsConfig()).getOrLoad() }
        scoped<DialogsConfig> { config("dialogs", abyss.dataPath, DialogsConfig()).getOrLoad() }
        scoped<World> { getWorld("world")!! }
        scopedOf(::NpcManager)
    }

    onEnable {
        val manager = get<NpcManager>()

        listeners(manager)
        abyss.plugin.launch {
            delay(10.seconds) //TODO correctly schedule loading when necessary
            runCatching { manager.initNpc() }.onFailure { it.printStackTrace() }
        }
    }
    mainCommand {
        "npcs" {
            "reload" {
                executes {
                    get<NpcManager>().initNpc()
                }
            }
        }
    }
}