package com.mineinabyss.features.npc.shopkeeping

import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.features.abyss
import com.mineinabyss.features.gondolas.GondolaFeature
import com.mineinabyss.features.gondolas.GondolaFeature.Context
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
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.plugin.listeners
import com.ticxo.modelengine.api.utils.config.ConfigManager
import com.mineinabyss.idofront.config.config
import org.bukkit.Bukkit.getWorld


object listenerSingleton {
    var sgl: NpcsConfig? = null
    var bstgth: MutableMap<Long, List<NpcEntity>>? = null
}

class ShopKeepingFeature : FeatureWithContext<ShopKeepingFeature.Context>(::Context) {

    class Context : Configurable<NpcsConfig> {
        override val configManager: IdofrontConfig<NpcsConfig> = config("npc", abyss.dataPath, NpcsConfig())
        val npcconfig by  config("npc", abyss.dataPath, NpcsConfig())
        val dialogsConfig by config("dialogs", abyss.dataPath, DialogsConfig())
        val manager = NpcManager(npcconfig, getWorld("world")!!, dialogsConfig)
        init {
            manager.initNpc()

        }
    }
    override fun FeatureDSL.enable() = gearyPaper.run {
//        println("Enabling Shopkeeping Feature")
        plugin.listeners(ShopKeepingListener())
        context.manager.initNpc()
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
