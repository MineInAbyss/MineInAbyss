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
        OkiboLineStation("GoldenBridge", Location(Bukkit.getWorld("world"), -491.0, 128.0, -31.0)),
        OkiboLineStation("GuildHQ", Location(Bukkit.getWorld("world"), -160.0, 135.0, -533.0)),
        OkiboLineStation("Big Tree", Location(Bukkit.getWorld("world"), 153.0, 130.0, 607.0))
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
                    var destination by optionArg(travelPoints.map { it.name }) { default = travelPoints.last().name }
                    "spawn" {
                        playerAction {
                            destination = if (station == destination) travelPoints.firstOrNull { it.name != station }?.name ?: station else destination
                            spawnOkiboCart(player, travelPoints.find { it.name == station }!!, travelPoints.find { it.name == destination }!!)
                        }
                    }
                }
            }
        }
    }
}
