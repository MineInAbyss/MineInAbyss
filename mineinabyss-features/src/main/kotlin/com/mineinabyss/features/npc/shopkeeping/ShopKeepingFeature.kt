package com.mineinabyss.features.npc.shopkeeping

import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.features.npc.shopkeeping.menu.ShopMainMenu
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class ShopKeepingFeature : Feature() {

    override fun FeatureDSL.enable() {
        plugin.listeners(ShopKeepingListener())

        mainCommand {
            "shops" {
                val shopKey by optionArg(options = ShopKeepers.getKeys().map { it.toString() }) {
                    parseErrorMessage = { "No such shopkeeper: $passed" }
                }
                playerAction {
                    val shopKeeper =
                        PrefabKey.of(shopKey).toEntityOrNull()?.get<ShopKeeper>() ?: return@playerAction
                    guiy { ShopMainMenu(player, shopKeeper) }
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
