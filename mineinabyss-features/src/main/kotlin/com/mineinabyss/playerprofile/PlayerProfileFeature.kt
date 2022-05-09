package com.mineinabyss.playerprofile

import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlayerProfileFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {

        commands {
            mineinabyss {
                "profile"(desc = "Opens a players profile") {
                    ensureSenderIsPlayer()
                    playerAction {
                        guiy { PlayerProfile(sender as Player, player) }
                    }
                }
            }
            tabCompletion {
                val list = mutableListOf<String>()
                for (p in Bukkit.getOnlinePlayers())
                    if (p != sender as Player) list.add(p.name)

                when (args.size) {
                    1 -> listOf("profile").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "profile" -> list
                            else -> null
                        }
                    }
                    else -> emptyList()
                }
            }
        }
    }
}