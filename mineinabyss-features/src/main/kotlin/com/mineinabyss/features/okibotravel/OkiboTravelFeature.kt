package com.mineinabyss.features.okibotravel

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Interaction
import org.bukkit.entity.TextDisplay
import java.util.*

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
        listeners(OkiboTravelListener(this@OkiboTravelFeature))

        val mapEntities = mutableSetOf<UUID>()
        commands {
            mineinabyss {
                "okibo" {
                    "map" {
                        playerAction {
                            mapEntities.forEach { Bukkit.getEntity(it)?.remove() }
                            mapEntities.clear()
                            okiboLine.config.okiboMaps.forEach { mapText ->
                                val station = okiboLine.config.okiboStations.firstOrNull { it.name == mapText.station } ?: return@forEach

                                val text = station.location.clone().subtract(1.0,0.0,0.0).spawn<TextDisplay> {
                                    text(mapText.text.miniMsg().font(Key.key(mapText.font)))
                                    isPersistent = false
                                    transformation = transformation.apply { scale.set(mapText.scale) }
                                    backgroundColor = Color.fromARGB(0,0,0,0)
                                    mapEntities += uniqueId
                                } ?: return@forEach

                                mapText.hitboxes.forEach { mapHitbox ->
                                    val hitbox = text.location.clone().add(mapHitbox.offset).spawn<Interaction> {
                                        interactionHeight = mapHitbox.hitbox.height.toFloat()
                                        interactionWidth = mapHitbox.hitbox.width.toFloat()
                                        isPersistent = false
                                        mapEntities += uniqueId

                                    }

                                    hitbox?.toGearyOrNull()?.set(mapHitbox)
                                    okiboLine.config.okiboStations.firstOrNull { it.name == mapHitbox.destStation }?.let { station ->
                                        hitbox?.toGearyOrNull()?.setPersisting(station)
                                    }
                                }
                            }
                            player.success("Okibo-Maps spawned")
                        }
                    }
                    "reload" {
                        action {
                            setupOkiboContext()
                            sender.success("Okibo-Context reloaded")
                        }
                    }
                    val station by optionArg(okiboLine.config.okiboStations.map { it.name }) { default = okiboLine.config.okiboStations.first().name }
                    "spawn" {
                        var destination by optionArg(okiboLine.config.okiboStations.map { it.name }) {
                            default = okiboLine.config.okiboStations.last().name
                        }
                        playerAction {
                            destination =
                                if (station == destination) okiboLine.config.okiboStations.firstOrNull { it.name != station }?.name
                                    ?: station else destination
                            spawnOkiboCart(
                                player,
                                okiboLine.config.okiboStations.find { it.name == station }!!,
                                okiboLine.config.okiboStations.find { it.name == destination }!!
                            )
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("okibo").filter { it.startsWith(args[0]) }
                    2 -> if (args[0] == "okibo") listOf("spawn", "reload", "map").filter { it.startsWith(args[1]) } else null
                    3 -> if (args[1] in listOf("gui", "spawn")) okiboLine.config.okiboStations.map { it.name }.filter { it.startsWith(args[2]) } else null
                    4 -> if (args[1] == "spawn") okiboLine.config.okiboStations.map { it.name }.filter { it.startsWith(args[3]) } else null
                    else -> null
                }
            }
        }
    }
}
