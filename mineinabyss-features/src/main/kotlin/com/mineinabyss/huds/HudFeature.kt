package com.mineinabyss.huds

import com.mineinabyss.components.huds.AlwaysShowAirHud
import com.mineinabyss.components.huds.AlwaysShowArmorHud
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.helpers.changeHudState
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("custom_hud")
class HudFeature(
    val healthElement: String = "health",
    val foodElement: String = "food",
    val hungerLayout: String = "hunger",
    val expElement: String = "exp",
    val armorLayout: String = "armor",
    val airLayout: String = "air",

    val absorptionLayout: String = "absorption",
    val witherLayout: String = "wither",
    val bleedingLayout: String = "bleeding",
    val poisonLayout: String = "poison",
    val freezingLayout: String = "freezing",

    val mountedLayout: String = "mounted",
    val balanceEmptyOffhandLayout: String = "balance_empty_offhand",
    val balanceOffhandLayout: String = "balance_offhand",

    val orthLayout: String = "orth",
    val edgeLayout: String = "edge_of_the_abyss",
    val forestLayout: String = "forest_of_temptation",
    val greatFaultLayout: String = "great_fault",
    val gobletsLayout: String = "goblets_of_giants",
    val seaLayout: String = "sea_of_corpses",
) : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        if (!abyss.isHappyHudEnabled) {
            logger.warning("HappyHUD is not enabled. HappyHud will not work.")
            return
        }

        listeners(HudListener(this@HudFeature))

        commands {
            mineinabyss {
                "hud" {
                    "air_bar" {
                        "toggleAlwaysOn" {
                            playerAction {
                                player.toGeary().let {
                                    if (it.has<AlwaysShowAirHud>())
                                        it.remove<AlwaysShowAirHud>()
                                    else it.add<AlwaysShowAirHud>()
                                    player.changeHudState(airLayout, it.has<AlwaysShowAirHud>())
                                    player.success("Air Bar was toggled ${if (it.has<AlwaysShowAirHud>()) "on" else "off"}")
                                }
                            }
                        }
                    }
                    "armor_bar" {
                        "toggleAlwaysOn" {
                            playerAction {
                                player.toGeary().let {
                                    if (it.has<AlwaysShowArmorHud>())
                                        it.remove<AlwaysShowArmorHud>()
                                    else it.add<AlwaysShowArmorHud>()
                                    player.changeHudState(armorLayout, it.has<AlwaysShowArmorHud>())
                                    player.success("Armor Bar was toggled ${if (it.has<AlwaysShowArmorHud>()) "on" else "off"}")
                                }
                            }
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("hud").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "hud" -> listOf("air_bar", "armor_bar").filter { it.startsWith(args[1]) }
                            else -> null
                        }
                    }
                    3 -> when (args[1]) {
                        "air_bar","armor_bar" -> listOf("toggleAlwaysOn").filter { it.startsWith(args[2]) }
                        else -> null
                    }

                    else -> null
                }
            }
        }
    }
}
