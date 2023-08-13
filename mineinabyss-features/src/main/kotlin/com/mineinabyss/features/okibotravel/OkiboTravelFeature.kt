package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboMap
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.features.okibotravel.menu.OkiboMainScreen
import com.mineinabyss.features.okibotravel.menu.spawnOkiboCart
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Display
import org.bukkit.entity.Interaction
import org.bukkit.entity.TextDisplay

@Serializable
@SerialName("okibotravel")
class OkiboTravelFeature : AbyssFeature {
    private fun setupOkiboContext() {
        DI.remove<OkiboLineContext>()
        DI.add<OkiboLineContext>(object : OkiboLineContext {
            override val okiboStations: Set<OkiboLineStation> by config("okiboStations") { abyss.plugin.fromPluginPath(loadDefault = true) }
        })
    }

    override fun MineInAbyssPlugin.enableFeature() {
        setupOkiboContext()
        listeners(OkiboTravelListener(this@OkiboTravelFeature))

        commands {
            mineinabyss {
                "okibo" {
                    "test" {
                        playerAction {
                            val okiboMap = player.world.spawn(player.location, TextDisplay::class.java) {
                                it.text("""
                                <font:orth_map>
                                
                                
                                
                                
                                
                                
                                
                                
                                
                                
                            """.trimIndent().miniMsg())
                                it.billboard = Display.Billboard.FIXED
                                it.isPersistent = false
                                it.isShadowed = false
                                it.transformation = it.transformation.apply { scale.set(2f, 2f, 2f) }
                            }
                            val okiboMapHitbox = player.world.spawn(player.location, Interaction::class.java) {
                                it.interactionHeight = 6.0f
                                it.interactionWidth = 6.0f
                            }
                            okiboMapHitbox.toGeary().add<OkiboMap>()

                        }
                    }
                    "reload" {
                        action {
                            setupOkiboContext()
                            sender.sendMessage("Okibo-Context reloaded".miniMsg())
                        }
                    }
                    val station by optionArg(okiboLine.okiboStations.map { it.name }) { default = okiboLine.okiboStations.first().name }
                    "gui" {
                        playerAction {
                            guiy { OkiboMainScreen(player, this@OkiboTravelFeature, OkiboTraveler(station)) }
                        }
                    }
                    "spawn" {
                        var destination by optionArg(okiboLine.okiboStations.map { it.name }) {
                            default = okiboLine.okiboStations.last().name
                        }
                        playerAction {
                            destination =
                                if (station == destination) okiboLine.okiboStations.firstOrNull { it.name != station }?.name
                                    ?: station else destination
                            spawnOkiboCart(
                                player,
                                okiboLine.okiboStations.find { it.name == station }!!,
                                okiboLine.okiboStations.find { it.name == destination }!!
                            )
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("okibo").filter { it.startsWith(args[0]) }
                    2 -> if (args[0] == "okibo") listOf("gui", "spawn").filter { it.startsWith(args[1]) } else null
                    3 -> if (args[0] == "okibo") okiboLine.okiboStations.map { it.name }.filter { it.startsWith(args[2]) } else null
                    4 -> if (args[1] == "spawn") okiboLine.okiboStations.map { it.name }.filter { it.startsWith(args[3]) } else null
                    else -> null
                }
            }
        }
    }
}
