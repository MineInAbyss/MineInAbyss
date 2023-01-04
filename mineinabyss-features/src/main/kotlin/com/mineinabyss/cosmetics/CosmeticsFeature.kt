package com.mineinabyss.cosmetics

import com.hibiscusmc.hmccosmetics.gui.Menus
import com.mineinabyss.helpers.cosmeticUser
import com.mineinabyss.helpers.hmcCosmetics
import com.mineinabyss.helpers.mcCosmetics
import com.mineinabyss.helpers.playGesture
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.isPluginEnabled
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.mineInAbyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("cosmetics")
class CosmeticsFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        if (!isPluginEnabled("HMCCosmetics")) return
        registerEvents(CosmeticListener())
        commands {
            mineinabyss {
                "cosmetic" {
                    "menu" {
                        val menu by stringArg()
                        playerAction {
                            if (hmcCosmetics.isEnabled)
                                Menus.getMenu(menu).openMenu(player.cosmeticUser)
                        }
                    }
                    "gesture" {
                        val gesture by stringArg()
                        playerAction {
                            if (!isPluginEnabled("MCCosmetics")) return@playerAction
                            (sender as Player).playGesture(gesture)
                            if (mineInAbyss.server.pluginManager.isPluginEnabled("MCCosmetics"))
                                (sender as Player).playGesture(gesture)
                        }
                    }
                }
            }
            tabCompletion {
                val emotes: MutableList<String> = ArrayList()

                if (isPluginEnabled("MCCosmetics")) {
                    for (gesture in mcCosmetics.gestureManager.allCosmetics) {
                        if ((sender as Player).hasPermission(gesture.permission))
                            emotes.add(gesture.key)
                    }
                }

                when (args.size) {
                    1 -> listOf("cosmetic").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "cosmetic" -> listOf("menu", "gesture").filter { it.startsWith(args[1]) }
                            else -> listOf()
                        }
                    }
                    3 -> {
                        when (args[1]) {
                            "gesture" -> emotes.filter { it.startsWith(args[2]) }
                            else -> listOf()
                        }
                    }
                    else -> listOf()
                }
            }
        }
    }
}
