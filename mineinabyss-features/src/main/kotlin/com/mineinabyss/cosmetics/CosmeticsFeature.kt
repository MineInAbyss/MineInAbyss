package com.mineinabyss.cosmetics

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.gui.Menus
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu
import com.mineinabyss.components.cosmetics.PersonalWardrobe
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.cosmeticUser
import com.mineinabyss.helpers.hmcCosmetics
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.isPluginEnabled
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player

@Serializable
@SerialName("cosmetics")
class CosmeticsFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        if (!isPluginEnabled("HMCCosmetics")) return
        HMCCosmeticsPlugin.setup()
        registerEvents(CosmeticListener())
        commands {
            mineinabyss {
                "cosmetics" {
                    "wardrobe" {
                        "personal" {
                            "viewer" {
                                val x by intArg { name = "X" }
                                val y by intArg { name = "Y" }
                                val z by intArg { name = "Z" }
                                playerAction {
                                    val location = Location(player.world, x.toDouble(), y.toDouble(), z.toDouble())
                                    player.toGeary {
                                        val wardrobe = this.get<PersonalWardrobe>()
                                        this.setPersisting(
                                            PersonalWardrobe(
                                                location,
                                                wardrobe?.wardrobeLocation,
                                                wardrobe?.leaveLocation
                                            )
                                        )
                                    }
                                }
                            }
                            "leave" {
                                val x by intArg { name = "X" }
                                val y by intArg { name = "Y" }
                                val z by intArg { name = "Z" }
                                playerAction {
                                    val location = Location(player.world, x.toDouble(), y.toDouble(), z.toDouble())
                                    player.toGeary {
                                        val wardrobe = this.get<PersonalWardrobe>()
                                        this.setPersisting(
                                            PersonalWardrobe(
                                                wardrobe?.viewerLocation,
                                                wardrobe?.wardrobeLocation,
                                                location
                                            )
                                        )
                                    }
                                }
                            }
                            "wardrobe" {
                                val x by intArg { name = "Y" }
                                val y by intArg { name = "Y" }
                                val z by intArg { name = "Z" }
                                playerAction {
                                    val location = Location(player.world, x.toDouble(), y.toDouble(), z.toDouble())
                                    player.toGeary {
                                        val wardrobe = this.get<PersonalWardrobe>()
                                        this.setPersisting(
                                            PersonalWardrobe(
                                                wardrobe?.viewerLocation,
                                                location,
                                                wardrobe?.leaveLocation
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        "open" {
                            playerAction {
                                player.toGeary().get<PersonalWardrobe>()?.let {
                                    player.cosmeticUser?.enterWardrobe(
                                        true,
                                        it.leaveLocation, it.viewerLocation, it.wardrobeLocation
                                    )
                                } ?: player.cosmeticUser?.enterWardrobe(
                                    true,
                                    player.location.clone(),
                                    player.location.clone().apply {
                                        when (player.facing) {
                                            BlockFace.NORTH -> yaw = 180f
                                            BlockFace.SOUTH -> yaw = 0f
                                            BlockFace.WEST -> yaw = 90f
                                            BlockFace.EAST -> yaw = 270f
                                            else -> {}
                                        }
                                    },
                                    player.location.clone().apply {
                                        when {
                                            player.facing.name.startsWith("NORTH") -> z -= 5
                                            player.facing.name.startsWith("SOUTH") -> z += 5
                                            player.facing.name.startsWith("WEST") -> x -= 5
                                            player.facing.name.startsWith("EAST") -> x += 5
                                        }
                                        pitch = 0f
                                    })

                            }
                        }
                    }
                    "menu" {
                        playerAction {
                            if (hmcCosmetics.isEnabled)
                                Menus.getDefaultMenu().openMenu(player.cosmeticUser)
                        }
                    }
                    "dye" {
                        val cosmeticSlot by optionArg(CosmeticSlot.values().map { it.name })
                        playerAction {
                            player.cosmeticUser?.let {
                                DyeMenu.openMenu(it, it.getCosmetic(CosmeticSlot.valueOf(cosmeticSlot)))
                            }
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("cosmetics").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "cosmetics" -> listOf("menu", "wardrobe", "dye").filter { it.startsWith(args[1]) }
                            else -> listOf()
                        }
                    }

                    3 -> {
                        when (args[1]) {
                            "wardrobe" -> listOf("personal", "open").filter { it.startsWith(args[2]) }
                            "dye" -> CosmeticSlot.values().map { it.name }.filter { it.uppercase().startsWith(args[2]) }
                            else -> listOf()
                        }
                    }

                    4 -> {
                        when (args[2]) {
                            "personal" -> listOf("leave", "wardrobe", "viewer").filter { it.startsWith(args[2]) }
                            else -> listOf()
                        }
                    }

                    5 -> {
                        when (args[2]) {
                            "personal" -> listOf((sender as? Player)?.location?.x.toString())
                            else -> listOf()
                        }
                    }

                    6 -> {
                        when (args[2]) {
                            "personal" -> listOf((sender as? Player)?.location?.y.toString())
                            else -> listOf()
                        }
                    }

                    7 -> {
                        when (args[2]) {
                            "personal" -> listOf((sender as? Player)?.location?.z.toString())
                            else -> listOf()
                        }
                    }

                    else -> listOf()
                }
            }
        }
    }
}
