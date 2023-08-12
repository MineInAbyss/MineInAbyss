package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.features.okibotravel.menu.OkiboMainScreen
import com.mineinabyss.features.okibotravel.menu.spawnOkiboCart
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location

@Serializable
@SerialName("okibotravel")
class OkiboTravelFeature(
    val travelPoints: Set<OkiboLineStation> = setOf(
        OkiboLineStation("GoldenBridge", Location(Bukkit.getWorld("world"), -617.0, 147.0, -68.0)),
        OkiboLineStation("GuildHQ", Location(Bukkit.getWorld("world"), -121.0, 130.0, -571.0)),
        OkiboLineStation("SlumDistrict", Location(Bukkit.getWorld("world"), -67.0, 131.0, 647.0))
    ),
) : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(OkiboTravelListener(this@OkiboTravelFeature))

        commands {
            mineinabyss {
                "okibo" {
                    val station by optionArg(travelPoints.map { it.name }) { default = travelPoints.first().name }
                    "gui" {
                        playerAction {
                            guiy { OkiboMainScreen(player, this@OkiboTravelFeature, OkiboTraveler(station)) }
                        }
                    }
                    "spawn" {
                        var destination by optionArg(travelPoints.map { it.name }) {
                            default = travelPoints.last().name
                        }
                        playerAction {
                            destination =
                                if (station == destination) travelPoints.firstOrNull { it.name != station }?.name
                                    ?: station else destination
                            spawnOkiboCart(
                                player,
                                travelPoints.find { it.name == station }!!,
                                travelPoints.find { it.name == destination }!!
                            )
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("okibo").filter { it.startsWith(args[0]) }
                    2 -> if (args[0] == "okibo") listOf("gui", "spawn").filter { it.startsWith(args[1]) } else null
                    3 -> if (args[0] == "okibo") travelPoints.map { it.name }.filter { it.startsWith(args[2]) } else null
                    4 -> if (args[1] == "spawn") travelPoints.map { it.name }.filter { it.startsWith(args[3]) } else null
                    else -> null
                }
            }
        }
    }
}
