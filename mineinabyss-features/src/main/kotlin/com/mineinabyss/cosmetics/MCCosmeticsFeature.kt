package com.mineinabyss.cosmetics

import com.mineinabyss.helpers.playGesture
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.mcCosmetics
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("cosmetics")
class MCCosmeticsFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(MCCosmeticsIntegration())
        commands {
            mineinabyss {
                "gesture" {
                    val gesture by stringArg()
                    playerAction {
                        val player = sender as Player
                        player.playGesture(gesture)
                    }
                }
            }
            tabCompletion {
                val emotes: MutableList<String> = ArrayList()
                for (gesture in mcCosmetics.gestureManager.allCosmetics) {
                    if ((this.sender as Player).hasPermission(gesture.permission))
                        emotes.add(gesture.key)
                }
                when (args.size) {
                    1 -> listOf(
                        "gesture"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "gesture" -> emotes
                            else -> listOf()
                        }
                    }
                    else -> listOf()
                }
            }
        }
    }
}