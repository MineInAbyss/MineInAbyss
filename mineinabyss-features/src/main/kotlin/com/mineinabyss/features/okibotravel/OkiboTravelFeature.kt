package com.mineinabyss.features.okibotravel

import com.mineinabyss.blocky.api.BlockyFurnitures
import com.mineinabyss.features.abyss
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
            removeNoticeboards()
            spawnNoticeboards()
            abyss.logger.s("Okibo-Context reloaded")
        })
        val okiboTravelListener = OkiboTravelListener()

        fun removeNoticeboards() {
            noticeBoardFurnitures.onEach { Bukkit.getEntity(it)?.remove() }.clear()
        }

        fun spawnNoticeboards() {
            config.okiboMaps.forEach { okiboMap ->
                val station = okiboMap.getStation ?: return@forEach
                val noticeBoardFurniture = okiboMap.noticeBoardFurniture ?: return@forEach
                val location = station.location.clone().add(okiboMap.offset).apply { yaw = okiboMap.yaw }.add(noticeBoardFurniture.offset)
                abyss.logger.w("Attempting to spawn NoticeBoard at ${station.name} | $location")
                location.world.getChunkAtAsync(location).thenAccept {
                    it.addPluginChunkTicket(abyss.plugin)
                    val noticeBoard = BlockyFurnitures.placeFurniture(noticeBoardFurniture.prefabKey, location, noticeBoardFurniture.yaw) ?: return@thenAccept
                    noticeBoard.isPersistent = false
                    noticeBoardFurnitures.add(noticeBoard.uniqueId)
                    abyss.logger.s("Spawned NoticeBoard at ${station.name} | $location")
                }
            }
        }
    }

    override val dependsOn = setOf("Train_Carts", "TCCoasters", "BKCommonLib", "Blocky")

    override fun FeatureDSL.enable() {
        plugin.listeners(context.okiboTravelListener)

        context.spawnNoticeboards()

        mainCommand {
            "okibo" {
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
                2 -> if (args[0] == "okibo") listOf("spawn")
                    .filter { it.startsWith(args[1]) } else null
                3 -> when {
                    args[1] in listOf("spawn") -> context.config.allStations.map { it.name }
                    else -> null
                }?.filter { it.startsWith(args[2]) }

                4 -> if (args[1] == "spawn") context.config.allStations.map { it.name }
                    .filter { it.startsWith(args[3]) } else null

                else -> null
            }
        }
    }

    override fun FeatureDSL.disable() {
        noticeBoardFurnitures.forEach { Bukkit.getEntity(it)?.remove() }
    }
}
