package com.mineinabyss.okiboline

import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.helpers.generateOkiboLineLocationImages
import com.mineinabyss.helpers.generateOkiboLineTransitionGIFs
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
import org.bukkit.Location
import org.bukkit.entity.Player

@Serializable
@SerialName("okibo_line")
class OkiboLineFeature(
    val okiboPoints: List<@Serializable(with = LocationSerializer::class) Location>,
    val okiboImagesFromURL: List<String> = emptyList(),
    val okiboImagesFromFilePath: List<String> = emptyList(),
) : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {

        if (okiboPoints.size < 2) {
            error("Okibo line must have at least 2 points")
        }

        generateOkiboLineLocationImages()
        generateOkiboLineTransitionGIFs()

        registerEvents(OkiboLineListener())
        commands {
            mineinabyss {
                "okibo_line"(desc = "Commands related to Okibo Line System in Orth") {
                    playerAction {
                        val player = sender as? Player ?: return@playerAction
                        if (!player.isInHub())
                            player.error("You must be in <gold>Orth</gold> to use this command.")
                        else guiy { OkiboLineMenu(player) }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("okibo_line").filter { it.startsWith(args[0]) }
                    else -> null
                }
            }
        }
    }
}
