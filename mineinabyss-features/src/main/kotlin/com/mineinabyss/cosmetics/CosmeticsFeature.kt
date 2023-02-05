package com.mineinabyss.cosmetics

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.gui.Menus
import com.mineinabyss.helpers.cosmeticUser
import com.mineinabyss.helpers.hmcCosmetics
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.isPluginEnabled
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("cosmetics")
class CosmeticsFeature(
    private val defaultMenu: String = "defaultmenu"
) : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        if (!isPluginEnabled("HMCCosmetics")) return
        HMCCosmeticsPlugin.setup()
        registerEvents(CosmeticListener())
        commands {
            mineinabyss {
                "cosmetic" {
                    "menu" {
                        playerAction {
                            if (hmcCosmetics.isEnabled)
                                Menus.getMenu(defaultMenu).openMenu(player.cosmeticUser)
                        }
                    }
                    "gesture" {
                        //TODO Wait until HMCCosmetics gets gestures
                        /*val gesture by stringArg()
                        playerAction {
                            if (!isPluginEnabled("MCCosmetics")) return@playerAction
                            (sender as Player).playGesture(gesture)
                            if (mineInAbyss.server.pluginManager.isPluginEnabled("MCCosmetics"))
                                (sender as Player).playGesture(gesture)
                        }*/
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("cosmetic").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "cosmetic" -> listOf("menu").filter { it.startsWith(args[1]) }
                            else -> listOf()
                        }
                    }
                    else -> listOf()
                }
            }
        }
    }
}
