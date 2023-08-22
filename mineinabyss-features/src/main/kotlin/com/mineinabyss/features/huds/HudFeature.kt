package com.mineinabyss.features.huds

import com.mineinabyss.components.huds.ReturnVanillaHud
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
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
    val vanillaHudLayout: String = "vanillaLayout",
    val vanillaPackName: String = "survivalpack",
    val hudPackName: String = "survivalpack_happyhud",

    val hungerLayout: String = "hunger",
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
        if (!abyss.isHappyHUDEnabled) {
            logger.warning("HappyHUD is not enabled. HappyHud will not work.")
            return
        }

        listeners(HudListener())

        commands {
            mineinabyss {
                "hud" {
                    "toggleCustomHud" {
                        playerAction {
                            when {
                                player.toGeary().has<ReturnVanillaHud>() -> player.toGeary().remove<ReturnVanillaHud>()
                                else -> player.toGeary().add<ReturnVanillaHud>()
                            }

                            player.success("Toggled custom hud of vanilla elements ${if (player.toGeary().has<ReturnVanillaHud>()) "off" else "on"}")
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("hud").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "hud" -> listOf("toggleCustomHud").filter { it.startsWith(args[1]) }
                            else -> null
                        }
                    }

                    else -> null
                }
            }
        }
    }
}
