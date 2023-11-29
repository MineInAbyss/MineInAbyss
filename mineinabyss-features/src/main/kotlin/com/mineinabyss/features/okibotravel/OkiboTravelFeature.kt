package com.mineinabyss.features.okibotravel

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.Bukkit

class OkiboTravelFeature : FeatureWithContext<OkiboTravelFeature.Context>(::Context) {
    class Context : Configurable<OkiboTravelConfig> {
        override val configManager = config("okiboTravel", abyss.dataPath, OkiboTravelConfig(), onReload = {
            spawnOkiboMaps()
            logSuccess("Okibo-Context reloaded")
        })
        val okiboTravelListener = OkiboTravelListener()
    }

    override val dependsOn = setOf("Train_Carts", "TCCoasters", "BKCommonLib", "ProtocolBurrito")

    override fun FeatureDSL.enable() {
        plugin.listeners(context.okiboTravelListener)
        spawnOkiboMaps()

        mainCommand {
            "okibo" {
                "map" {
                    "spawn" {
                        playerAction {
                            spawnOkiboMaps()
                            player.success("Okibo-Maps spawned")
                        }
                    }
                    "clear" {
                        playerAction {
                            player.clearOkiboMaps()
                            player.success("Okibo-Maps cleared")
                        }
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
                2 -> if (args[0] == "okibo") listOf("spawn", "map")
                    .filter { it.startsWith(args[1]) } else null

                3 -> when {
                    args[1] in listOf("spawn") -> context.config.allStations.map { it.name }
                    args[1] == "map" -> listOf("spawn", "clear")
                    else -> null
                }?.filter { it.startsWith(args[2]) }

                4 -> if (args[1] == "spawn") context.config.allStations.map { it.name }
                    .filter { it.startsWith(args[3]) } else null

                else -> null
            }
        }
    }

    override fun FeatureDSL.disable() {
        Bukkit.getOnlinePlayers().forEach { it.clearOkiboMaps() }
        mapEntities.clear()
        hitboxEntities.clear()
        hitboxIconEntities.clear()
    }
}
