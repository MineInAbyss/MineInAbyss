package com.mineinabyss.relics

import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import com.mineinabyss.relics.sickle.SickleListener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.meta.CompassMeta

@Serializable
@SerialName("relics")
class RelicsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {

        geary {
            systems(
                ShowDepthListener(),
                StarCompassListener()
            )
        }
        registerEvents(SickleListener())

        commands {
            mineinabyss {
                "dim" {
                    playerAction {
                        if (player.inventory.itemInMainHand.type != Material.COMPASS) return@playerAction
                        (player.inventory.itemInMainHand.itemMeta as CompassMeta).lodestone.broadcastVal()
                    }
                }
            }
        }
    }
}
