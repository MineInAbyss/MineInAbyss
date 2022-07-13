package com.mineinabyss.imagegenerator

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.helpers.*
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.mineInAbyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title

@Serializable
@SerialName("image_generator")
class ImageGenFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {

        commands {
            mineinabyss {
                "imagegen" {
                    "guildMenuTest" {
                        "chat" {
                            broadcast(guildMenu)
                        }
                        "menu" {
                            playerAction {
                                mineInAbyss.launch(mineInAbyss.asyncDispatcher) { guiy { test(player, guildMenu) } }
                                broadcast(guildMenu)
                            }
                        }
                        "title" {
                            playerAction {
                                player.showTitle(Title.title(guildMenu, Component.text("")))
                            }
                        }
                    }
                    "test" {
                        "test" {
                            playerAction {
                                val gif = generateGif() ?: return@playerAction
                                gif.next
                                val t = convertToImageComponent(gif.next, Key.key("minecraft:cubes"))
                                mineInAbyss.launch(mineInAbyss.asyncDispatcher) { guiy { test(player, t) } }
                            }
                        }
                        "chat" {
                            broadcast(gifComponent)
                        }
                        "menu" {
                            playerAction {
                                mineInAbyss.launch(mineInAbyss.asyncDispatcher) { guiy { test(player, gifComponent) } }
                            }
                        }
                        "title" {
                            playerAction {
                                player.showTitle(Title.title(gifComponent, Component.text("")))
                            }
                        }
                    }
                }
            }
            tabCompletion {

                when (args.size) {
                    1 -> listOf("imagegen").filter { it.startsWith(args[0]) }
                    2 -> when (args[0]) {
                        "imagegen" -> listOf("guildMenuTest", "test").filter { it.startsWith(args[1]) }
                        else -> listOf()
                    }
                    3 ->
                        when (args[0]) {
                            "imagegen" -> listOf("chat", "menu", "title").filter { it.startsWith(args[2]) }
                            else -> null
                        }
                    else -> emptyList()
                }
            }
        }
    }
}
