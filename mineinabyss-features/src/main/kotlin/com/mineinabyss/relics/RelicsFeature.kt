package com.mineinabyss.relics

import com.mineinabyss.components.helpers.HideBossBarCompass
import com.mineinabyss.components.helpers.HideDepthMeterHud
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import com.mineinabyss.relics.depthmeter.DepthHudSystem
import com.mineinabyss.relics.depthmeter.RemoveDepthMeterHud
import com.mineinabyss.relics.depthmeter.ToggleDepthHudSystem
import com.mineinabyss.relics.sickle.HarvestListener
import com.mineinabyss.relics.sickle.SickleListener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("relics")
class RelicsFeature(
    val depthHudId: String = "depth"
) : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {

        geary {
            systems(
                RemoveStarCompassBar(),
                ShowDepthSystem(),
                DepthHudSystem(this@RelicsFeature),
                RemoveDepthMeterHud(this@RelicsFeature),
                ToggleDepthHudSystem(this@RelicsFeature),
                StarCompassSystem(),
                HarvestListener()
            )
        }
        registerEvents(SickleListener())

        commands {
            mineinabyss {
                "relics"(desc = "Commands for relic-related stuff") {
                    "star_compass"(desc = "Commands related to the Star Compass") {
                        "toggle" {
                            playerAction {
                                val geary = (sender as Player).toGeary()
                                if (geary.has<HideBossBarCompass>()) geary.remove<HideBossBarCompass>()
                                else geary.setPersisting(HideBossBarCompass())
                            }
                        }
                    }
                    "depth_meter"(desc = "Commands related to the Depth-Meter") {
                        "toggle" {
                            playerAction {
                                val geary = (sender as Player).toGeary()
                                if (geary.has<HideDepthMeterHud>()) geary.remove<HideDepthMeterHud>()
                                else geary.setPersisting(HideDepthMeterHud())
                            }
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "relics"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "relics" -> listOf("star_compass", "depth_meter")
                            else -> listOf()
                        }
                    }
                    3 -> when (args[1]) {
                        "star_compass", "depth_meter" -> listOf("toggle")
                        else -> listOf()
                    }
                    else -> listOf()
                }
            }
        }
    }
}
