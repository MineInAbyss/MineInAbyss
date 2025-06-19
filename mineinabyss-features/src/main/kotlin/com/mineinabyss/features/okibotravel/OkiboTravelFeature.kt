package com.mineinabyss.features.okibotravel

import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.di.Features.okiboLine
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.Bukkit

class OkiboTravelFeature : FeatureWithContext<OkiboTravelFeature.Context>(::Context) {
    class Context : Configurable<OkiboTravelConfig> {
        override val configManager = config("okiboTravel", abyss.dataPath, OkiboTravelConfig(), onReload = {
            removeOkiboMaps()

            mapEntities.clear()
            hitboxEntities.clear()
            hitboxIconEntities.clear()

            spawnOkiboMaps()
        }, onLoad = { config ->
            abyss.logger.s("Reloaded OkiboLine!")
        })
        val okiboTravelListener = OkiboTravelListener()
    }

    override val dependsOn = setOf("BKCommonLib", "Train_Carts", "TCCoasters")

    override fun FeatureDSL.enable() {
        plugin.listeners(context.okiboTravelListener)

        mainCommand {
            "okibo" {
                "reload" {
                    context.configManager.reload()
                }
                val station by optionArg(context.config.allStations.map { it.id }) {
                    default = context.config.okiboStations.first().id
                }
                "spawn" {
                    var destination by optionArg(context.config.allStations.map { it.id }) {
                        default = context.config.okiboStations.last().id
                    }
                    playerAction {
                        destination =
                            if (station == destination) context.config.okiboStations.firstOrNull { it.id != station }?.id
                                ?: station else destination
                        spawnOkiboCart(
                            player,
                            context.config.allStations.find { it.id == station }
                                ?: return@playerAction player.error("Invalid station!"),
                            context.config.allStations.find { it.id == destination }
                                ?: return@playerAction player.error("Invalid destination!")
                        )
                    }
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("okibo").filter { it.startsWith(args[0]) }
                2 -> if (args[0] == "okibo") listOf("spawn", "reload")
                    .filter { it.startsWith(args[1]) } else null
                3 -> when {
                    args[1] in listOf("spawn") -> context.config.allStations.map { it.id }
                    else -> null
                }?.filter { it.startsWith(args[2], true) }

                4 -> if (args[1] == "spawn") context.config.allStations.map { it.id }
                    .filter { it.startsWith(args[3], true) } else null

                else -> null
            }
        }
    }
}
