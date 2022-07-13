package com.mineinabyss.cosmetics

import com.mineinabyss.helpers.hmcCosmetics
import com.mineinabyss.helpers.mcCosmetics
import com.mineinabyss.helpers.playGesture
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.mineInAbyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player

@Serializable
@SerialName("cosmetics")
class CosmeticsFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(CosmeticListener())
        commands {
            mineinabyss {
                "cosmetic" {
                    "menu" {
                        playerAction {
                            if (hmcCosmetics.isEnabled) hmcCosmetics.cosmeticsMenu.openDefault(sender as HumanEntity)
                            if (mineInAbyss.server.pluginManager.isPluginEnabled("HMCCosmetics"))
                                hmcCosmetics.cosmeticsMenu.openDefault(sender as HumanEntity)
                        }
                    }
                    "gesture" {
                        val gesture by stringArg()
                        playerAction {
                            (sender as Player).playGesture(gesture)
                            if (mineInAbyss.server.pluginManager.isPluginEnabled("MCCosmetics"))
                                (sender as Player).playGesture(gesture)
                        }
                    }
                }
            }
            tabCompletion {
                val emotes: MutableList<String> = ArrayList()

                if (mineInAbyss.server.pluginManager.isPluginEnabled("MCCosmetics")) {
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
