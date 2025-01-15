package com.mineinabyss.features.okibotravel

import com.mineinabyss.blocky.api.BlockyFurnitures
import com.mineinabyss.blocky.components.core.BlockyFurniture
import com.mineinabyss.components.okibotravel.OkiboMap
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay

class OkiboTravelFeature : FeatureWithContext<OkiboTravelFeature.Context>(::Context) {
    class Context : Configurable<OkiboTravelConfig> {
        override val configManager = config("okiboTravel", abyss.dataPath, OkiboTravelConfig(), onReload = {
            noticeBoardFurnitures.onEach { Bukkit.getEntity(it.key)?.remove() }.clear()
            mapEntities.clear()
            hitboxEntities.clear()
            hitboxIconEntities.clear()
        }, onLoad = { config ->
            config.spawnNoticeboards()
            abyss.logger.s("Reloaded OkiboLine!")
        })
        val okiboTravelListener = OkiboTravelListener()

        companion object {

            fun OkiboTravelConfig.spawnNoticeboards() {
                okiboMaps.forEach { okiboMap ->
                    val station = okiboStations.firstOrNull { it.name == okiboMap.station } ?: return@forEach
                    val noticeBoardFurniture = okiboMap.noticeBoardFurniture ?: return@forEach
                    val location = station.location.clone().add(okiboMap.offset).apply { yaw = okiboMap.yaw }.add(noticeBoardFurniture.offset).apply {
                        yaw = noticeBoardFurniture.yaw
                    }
                    abyss.logger.w("Attempting to spawn NoticeBoard at ${station.name} | $location")
                    location.world.getChunkAtAsync(location).thenAccept {
                        it.addPluginChunkTicket(abyss.plugin)
                        BlockyFurnitures.placeFurniture(noticeBoardFurniture.prefabKey, location)?.toGearyOrNull()?.set(okiboMap) ?: return@thenAccept
                        abyss.logger.s("Spawned NoticeBoard at ${station.name} | $location")
                    }
                }
            }
        }
    }

    private fun Geary.createNoticeboardTracker() = observe<OnSet>()
        .involving<ItemDisplay, BlockyFurniture, OkiboMap>()
        .exec(query<ItemDisplay, BlockyFurniture, OkiboMap>()) { (itemDisplay, _, okiboMap) ->
            itemDisplay.isPersistent = false
            noticeBoardFurnitures[itemDisplay.uniqueId] = okiboMap.station
        }

    override val dependsOn = setOf("BKCommonLib", "Train_Carts", "TCCoasters", "Blocky")

    override fun FeatureDSL.enable() {
        plugin.listeners(context.okiboTravelListener)

        gearyPaper.worldManager.global.createNoticeboardTracker()

        mainCommand {
            "okibo" {
                "reload" {
                    context.configManager.reload()
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
                2 -> if (args[0] == "okibo") listOf("spawn", "reload")
                    .filter { it.startsWith(args[1]) } else null
                3 -> when {
                    args[1] in listOf("spawn") -> context.config.allStations.map { it.name }
                    else -> null
                }?.filter { it.startsWith(args[2], true) }

                4 -> if (args[1] == "spawn") context.config.allStations.map { it.name }
                    .filter { it.startsWith(args[3], true) } else null

                else -> null
            }
        }
    }
}
