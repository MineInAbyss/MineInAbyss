package com.mineinabyss.npc.shopkeeping

import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.npc.shopkeeping.ShopKeeperQuery.key
import com.mineinabyss.npc.shopkeeping.menu.ShopMainMenu
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("shopkeeping")
class ShopKeepingFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(ShopKeepingListener())

        commands {
            mineinabyss {
                "shops" {
                    val shopKey by optionArg(options = ShopKeeperQuery.getKeys().map { it.toString() }) {
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
                            "shops" -> ShopKeeperQuery.filter {
                                val arg = args[1].lowercase()
                                it.key.key.startsWith(arg) || it.key.full.startsWith(arg)
                            }.map { it.key.toString() }

                            else -> null
                        }
                    }

                    else -> null
                }
            }
        }
    }
}
