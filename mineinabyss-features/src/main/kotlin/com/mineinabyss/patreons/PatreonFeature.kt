package com.mineinabyss.patreons

import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Serializable
@SerialName("patreon")
class PatreonFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {

        geary {
            systems()
        }
        registerEvents(PatreonListener())

        commands {
            mineinabyss {
                "patreon"(desc = "Patreon-supporter related commands") {
                    "prefix"(desc = "Change your prefix emote") {
                        "remove" {
                            playerAction {
                                val player = sender as Player
                                val console = Bukkit.getServer().consoleSender
                                Bukkit.dispatchCommand(console, "luckperms user ${player.name} meta clear prefix")
                            }
                        }
                        "set" {
                            val emote by stringArg()
                            val locs = listOf(
                                "global",
                                "orth",
                                "layerone",
                                "layertwo",
                                "layerthree",
                                "layerfour",
                                "layerfive"
                            )
                            val loc by optionArg(locs) {
                                default = "global"
                                parseErrorMessage = { "No such enchantment: $passed. \nAvailable ones are: \n$locs" }
                            }
                            playerAction {
                                val player = sender as Player
                                val console = Bukkit.getServer().consoleSender
                                if (loc == "global") Bukkit.dispatchCommand(
                                    console,
                                    "luckperms user ${player.name} meta setprefix :$emote:"
                                )
                                else Bukkit.dispatchCommand(
                                    console,
                                    "luckperms user ${player.name} meta setprefix :$emote: worldguard:region=$loc"
                                )
                            }
                        }
                    }
                }
            }
            val locs =
                listOf(
                    "global",
                    "orth",
                    "layerone",
                    "layertwo",
                    "layerthree",
                    "layerfour",
                    "layerfive"
                )
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "patreon"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "patreon" -> listOf("prefix")
                            else -> null
                        }
                    }
                    3 -> {
                        when (args[1]) {
                            "prefix" -> listOf("remove", "set")
                            else -> null
                        }
                    }
                    4 -> {
                        when (args[2]) {
                            "set" -> listOf("kekw", "pogo", "pogyou", "pog")
                            else -> null
                        }
                    }
                    5 -> {
                        when (args[2]) {
                            "set" -> locs
                            else -> null
                        }
                    }
                    else -> null
                }
            }
        }

    }
}