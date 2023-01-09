package com.mineinabyss.huds

import com.mineinabyss.components.huds.AlwaysShowAirHud
import com.mineinabyss.components.huds.ReturnVanillaHud
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.changeHudState
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.isPluginEnabled
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit

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
        if (!isPluginEnabled("HappyHUD")) {
            logger.warning("HappyHUD is not enabled. HappyHud will not work.")
            return
        }

        registerEvents(HudListener(this@HudFeature))

        commands {
            mineinabyss {
                "hud" {
                    "toggleCustomHud" {
                        //TODO Check if /resetpack is needed first
                        //TODO Check if theres api over using commands
                        playerAction {
                            player.toGeary {

                                if (has<ReturnVanillaHud>()) {
                                    remove<ReturnVanillaHud>()
                                    //remove<AlwaysShowAirHud>()
                                    //remove<AlwaysShowArmorHud>()
                                    player.performCommand("/usepack $hudPackName ${player.name}")
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"/usepack $hudPackName ${player.name}")

                                } else {
                                    add<ReturnVanillaHud>()
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"/usepack $vanillaPackName ${player.name}")
                                }

                                player.changeHudState(vanillaHudLayout, has<AlwaysShowAirHud>())
                                player.success("Toggled custom hud of vanilla elements ${if (has<AlwaysShowAirHud>()) "on" else "off"}")
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
