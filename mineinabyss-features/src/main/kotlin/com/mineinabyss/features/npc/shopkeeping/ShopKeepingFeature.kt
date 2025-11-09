package com.mineinabyss.features.npc.shopkeeping

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.features.abyss
import com.mineinabyss.features.npc.NpcAction.DialogsConfig
import com.mineinabyss.features.npc.NpcEntity
import com.mineinabyss.features.npc.NpcManager
import com.mineinabyss.features.npc.NpcsConfig
import com.mineinabyss.features.npc.shopkeeping.menu.ShopMainMenu
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.IdofrontConfig
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.plugin.listeners
import kotlinx.coroutines.delay
import org.bukkit.Bukkit.getWorld
import kotlin.time.Duration.Companion.seconds


object listenerSingleton {
    var sgl: NpcsConfig? = null
    var bstgth: MutableMap<Long, List<NpcEntity>> = mutableMapOf()
}

val ShopKeepingFeature = feature("shopkeepers") {
    override val dependsOn: Set<String> = setOf("ModelEngine")

    class Context : Configurable<NpcsConfig> {
        override val configManager: IdofrontConfig<NpcsConfig> = config("npc", abyss.dataPath, NpcsConfig())
        val npcconfig by config("npc", abyss.dataPath, NpcsConfig())
        val dialogsConfig by config("dialogs", abyss.dataPath, DialogsConfig())
        val manager = NpcManager(npcconfig, getWorld("world")!!, dialogsConfig)
    }

    override fun FeatureDSL.enable() = gearyPaper.run {
//        println("Enabling Shopkeeping Feature")
        plugin.listeners(ShopKeepingListener())
        plugin.launch {
            delay(10.seconds)
            runCatching { context.manager.initNpc() }.onFailure { it.printStackTrace() }
        }
        plugin.listeners(context.manager)
//        println("dialogconfig keys are ${context.dialogsConfig.configs.keys}")
//        val manager = NpcManager(context.npcconfig, getWorld("world")!!, context.dialogsConfig)
//        manager.initNpc()
//        plugin.listeners(manager)
//        println("Shopkeeping Feature Enabled")
        mainCommand {
            "test" {
                playerAction {
                    context.npcconfig.npcs.values.forEach {
                        player.sendMessage("${it.displayName} - ${it.id}")
                    }
                }
            }
            "shops" {
                val shopKey by optionArg(options = ShopKeepers.getKeys().map { it.toString() }) {
                    parseErrorMessage = { "No such shopkeeper: $passed" }
                }
                playerAction {
                    player.withGeary {
                        val shopKeeper = PrefabKey.of(shopKey).toEntityOrNull()?.get<ShopKeeper>() ?: return@playerAction
                        guiy(player) { ShopMainMenu(player, shopKeeper) }
                    }
                }
            }
            "npcs" {
                "reload" {
                    action {
                        context.manager.initNpc()
                    }
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("shops").filter { it.startsWith(args[0]) }
                2 -> {
                    when (args[0]) {
                        "shops" -> ShopKeepers.getKeys().filter { key ->
                            val arg = args[1].lowercase()
                            key.key.startsWith(arg) || key.full.startsWith(arg)
                        }.map { it.key }

                        else -> null
                    }
                }

                else -> null
            }
        }
    }
}
