package com.mineinabyss.features.okibotravel

import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeatureWithContext
import com.mineinabyss.mineinabyss.core.Configurable
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss

class OkiboTravelFeature : AbyssFeatureWithContext<OkiboTravelFeature.Context>(Context::class) {
    class Context : Configurable<OkiboTravelConfig> {
        override val configManager = config("okiboTravel", abyss.dataPath, OkiboTravelConfig(), onReload = {
            spawnOkiboMaps()
            logSuccess("Okibo-Context reloaded")
        })
        val okiboTravelListener = OkiboTravelListener()
    }

    override fun createContext() = Context()

    override val dependsOn = setOf("Train_Carts", "TCCoasters")

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(context.okiboTravelListener)
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
                    val station by optionArg(context.config.allStations.map { it.name }) {
                        default = context.config.okiboStations.first().name
                    }
                    "spawn" {
                        var destination by optionArg(context.config.allStations.map { it.name }) {
                            default = context.config.okiboStations.last().name
                        }
                        playerAction {
                            destination =
                                if (station == destination) context.config.okiboStations.firstOrNull { it.name != station }?.name
                                    ?: station else destination
                            spawnOkiboCart(
                                player,
                                context.config.allStations.find { it.name == station }
                                    ?: return@playerAction player.error("Invalid station!"),
                                context.config.allStations.find { it.name == destination }
                                    ?: return@playerAction player.error("Invalid destination!")
                            )
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("okibo").filter { it.startsWith(args[0]) }
                    2 -> if (args[0] == "okibo") listOf(
                        "spawn",
                        "reload",
                        "map"
                    ).filter { it.startsWith(args[1]) } else null

                    3 -> if (args[1] in listOf("gui", "spawn")) context.config.allStations.map { it.name }
                        .filter { it.startsWith(args[2]) } else null

                    4 -> if (args[1] == "spawn") context.config.allStations.map { it.name }
                        .filter { it.startsWith(args[3]) } else null

                    else -> null
                }
            }
        }
    }
}
