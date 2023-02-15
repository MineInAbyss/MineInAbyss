package com.mineinabyss.cosmetics

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.gui.Menus
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu
import com.mineinabyss.components.cosmetics.PersonalWardrobe
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.cosmeticUser
import com.mineinabyss.helpers.hmcCosmetics
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.isPluginEnabled
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.block.BlockFace

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
                            //TODO Add distance checks for difference between existing, non-null locations, and set locations
                            "viewer" {
                                playerAction {
                                    player.toGeary {
                                        val wardrobe = this.get<PersonalWardrobe>()
                                        this.setPersisting(
                                            PersonalWardrobe(
                                                player.location,
                                                wardrobe?.npcLocation,
                                                wardrobe?.leaveLocation
                                            )
                                        )
                                    }
                                    player.success("Set viewer-location for Personal Wardrobe")
                                }
                            }
                            "leave" {
                                playerAction {
                                    player.toGeary {
                                        val wardrobe = this.get<PersonalWardrobe>()
                                        this.setPersisting(
                                            PersonalWardrobe(
                                                wardrobe?.viewerLocation,
                                                wardrobe?.npcLocation,
                                                player.location
                                            )
                                        )
                                    }
                                    player.success("Set leave-location for Personal Wardrobe")
                                }
                            }
                            "npc" {
                                playerAction {
                                    player.toGeary {
                                        val wardrobe = this.get<PersonalWardrobe>()
                                        this.setPersisting(
                                            PersonalWardrobe(
                                                wardrobe?.viewerLocation,
                                                player.location,
                                                wardrobe?.leaveLocation
                                            )
                                        )
                                    }
                                    player.success("Set location of NPC for Personal Wardrobe")
                                }
                            }
                        }
                        "open" {
                            playerAction {
                                player.toGeary().get<PersonalWardrobe>()?.let {
                                    player.cosmeticUser?.enterWardrobe(
                                        false,
                                        player.location,
                                        it.viewerLocation,
                                        it.npcLocation
                                    )
                                } ?: player.cosmeticUser?.enterWardrobe(
                                    false,
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
                            "personal" -> listOf("leave", "npc", "viewer").filter { it.startsWith(args[3]) }
                            else -> listOf()
                        }
                    }

                    else -> listOf()
                }
            }
        }
    }
}
