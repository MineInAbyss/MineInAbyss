package com.mineinabyss.okibotravel

import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.serialization.LocationSerializer
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location

@Serializable
@SerialName("okibotravel")
class OkiboTravelFeature(
    val travelPoints: Set<OkiboTravelPoint> = setOf(
        OkiboTravelPoint("Gondola", Location(Bukkit.getWorld("world"), -491.0, 128.0, -31.0)),
        OkiboTravelPoint("Guild HQ", Location(Bukkit.getWorld("world"), -160.0, 135.0, -533.0)),
        OkiboTravelPoint("Big Tree", Location(Bukkit.getWorld("world"), 153.0, 130.0, 607.0))
    ),
) : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(OkiboTravelListener(this@OkiboTravelFeature))

        commands {
            mineinabyss {
                "okibo" {
                    playerAction {
                        guiy { OkiboTravelMenu(player, this@OkiboTravelFeature) }
                    }
                }
            }
        }
    }
}

@Serializable
data class OkiboTravelPoint(val name: String, val travelPoint: @Serializable(with = LocationSerializer::class) Location)
