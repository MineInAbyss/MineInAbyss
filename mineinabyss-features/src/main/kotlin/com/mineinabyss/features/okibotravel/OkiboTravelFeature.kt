package com.mineinabyss.features.okibotravel

import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("okibotravel")
class OkiboTravelFeature : AbyssFeature {
    private fun setupOkiboContext() {
        DI.remove<OkiboLineContext>()
        DI.add<OkiboLineContext>(object : OkiboLineContext {
            override val config: OkiboTravelConfig by config("okiboTravel") { abyss.plugin.fromPluginPath(loadDefault = true) }
        })
    }

    override fun MineInAbyssPlugin.enableFeature() {
        setupOkiboContext()
        listeners(OkiboTravelListener())
        spawnOkiboMaps()

        commands {
            mineinabyss {
                "okibo" {
                    "map" {
                        playerAction {
                            spawnOkiboMaps()
                            player.success("Okibo-Maps spawned")
                        }
                    }
                    "reload" {
                        action {
                            setupOkiboContext()
                            spawnOkiboMaps()
                            sender.success("Okibo-Context reloaded")
                        }
                    }
                    val station by optionArg(okiboLine.config.allStations.map { it.name }) { default = okiboLine.config.okiboStations.first().name }
                    "spawn" {
                        var destination by optionArg(okiboLine.config.allStations.map { it.name }) { default = okiboLine.config.okiboStations.last().name }
                        playerAction {
                            destination =
                                if (station == destination) okiboLine.config.okiboStations.firstOrNull { it.name != station }?.name
                                    ?: station else destination
                            spawnOkiboCart(
                                player,
                                okiboLine.config.allStations.find { it.name == station } ?: return@playerAction player.error("Invalid station!"),
                                okiboLine.config.allStations.find { it.name == destination } ?: return@playerAction player.error("Invalid destination!")
                            )
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("okibo").filter { it.startsWith(args[0]) }
                    2 -> if (args[0] == "okibo") listOf("spawn", "reload", "map").filter { it.startsWith(args[1]) } else null
                    3 -> if (args[1] in listOf("gui", "spawn")) okiboLine.config.allStations.map { it.name }.filter { it.startsWith(args[2]) } else null
                    4 -> if (args[1] == "spawn") okiboLine.config.allStations.map { it.name }.filter { it.startsWith(args[3]) } else null
                    else -> null
                }
            }
        }
    }
}
