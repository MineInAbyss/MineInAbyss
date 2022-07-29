package com.mineinabyss.okiboline

import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.helpers.generateOkiboLineLocationImages
import com.mineinabyss.hubstorage.isInHub
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.okiboline.menus.OkiboLineMenu
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

@Serializable
@SerialName("okibo_line")
class OkiboLineFeature(
    private val okiboPoints: List<@Serializable(with = LocationSerializer::class) Location> = listOf(
        Location(Bukkit.getWorlds().first(), 0.0, 0.0, 0.0),
        Location(Bukkit.getWorlds().first(), 10.0, 0.0, 0.0),
        Location(Bukkit.getWorlds().first(), 10.0, 5.0, 10.0),
    ),
    val okiboImageFileNames: List<String> = listOf(
        "point1.png",
        "point2.png",
        "point3.png",
    ),
    val okiboImagesFromFilePath: List<String> = emptyList(),
) : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {

        if (okiboPoints.size < 2) {
            error("Okibo line must have at least 2 points")
        }

        generateOkiboLineLocationImages(this@OkiboLineFeature)

        registerEvents(OkiboLineListener())
        commands {
            mineinabyss {
                "okibo_line"(desc = "Commands related to Okibo Line System in Orth") {
                    "menu" {
                        playerAction {
                            val player = sender as? Player ?: return@playerAction
                            if (!player.isInHub())
                                player.error("You must be in <gold>Orth</gold> to use this command.")
                            else guiy { OkiboLineMenu(player) }
                        }
                    }
                    "regen" {
                        action {
                            generateOkiboLineLocationImages(this@OkiboLineFeature)
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("okibo_line").filter { it.startsWith(args[0]) }
                    2 -> listOf("menu", "regen").filter { it.startsWith(args[1]) }
                    else -> null
                }
            }
        }
    }
}
