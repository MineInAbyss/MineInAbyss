package com.mineinabyss.cosmetics

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.config.Settings
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings
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
class CosmeticsFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        if (!isPluginEnabled("HMCCosmetics")) return
        HMCCosmeticsPlugin.setup()
        registerEvents(CosmeticListener())
        commands {
            mineinabyss {
                "cosmetic" {
                    "wardrobe" {
                        playerAction {
                            val viewerLoc = WardrobeSettings.getViewerLocation()
                            val wardrobeLoc = WardrobeSettings.getWardrobeLocation()
                            val leaveLoc = WardrobeSettings.getLeaveLocation()

                            WardrobeSettings.setViewerLocation(player.location.add(5.0, 0.0, 0.0))
                            WardrobeSettings.setWardrobeLocation(player.location)
                            WardrobeSettings.setLeaveLocation(player.location.apply { this.yaw = -this.yaw })
                            WardrobeSettings.isApplyCosmeticsOnClose()

                            player.cosmeticUser?.enterWardrobe()
                            //Reset back to the config ones
                            WardrobeSettings.setLeaveLocation(leaveLoc)
                            WardrobeSettings.setViewerLocation(viewerLoc)
                            WardrobeSettings.setWardrobeLocation(wardrobeLoc)
                        }
                    }
                    "menu" {
                        playerAction {
                            if (hmcCosmetics.isEnabled)
                                Menus.getMenu(Settings.getDefaultMenu()).openMenu(player.cosmeticUser)
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("cosmetic").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "cosmetic" -> listOf("menu", "wardrobe").filter { it.startsWith(args[1]) }
                            else -> listOf()
                        }
                    }
                    else -> listOf()
                }
            }
        }
    }
}
