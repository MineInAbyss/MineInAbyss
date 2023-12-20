package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.gui.Menus
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu
import com.mineinabyss.features.helpers.cosmeticUser
import com.mineinabyss.features.helpers.hmcCosmetics
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.warn
import com.mineinabyss.idofront.plugin.listeners
import kotlinx.serialization.Serializable

class CosmeticsFeature(val config: Config) : Feature() {
    @Serializable
    class Config {
        val enabled = false
        val equipBackpacks: Boolean = false
        val defaultBackpack: String = "backpack"
    }

    override val dependsOn: Set<String> = setOf("HMCCosmetics")
    override fun FeatureDSL.enable() {
        if (config.equipBackpacks) plugin.listeners(CosmeticListener(config))
        TypeMiaCosmetic()
        HMCCosmeticsPlugin.setup()

        // Makes backpacks equip/unequipable via player interaction
        // Make sure everything works before enabling it
        plugin.listeners(VendorListener())

        mainCommand {
            "cosmetics" {
                "wardrobe" {
                    action { sender.warn("This command is not fully implemented yet!") }
                    /*"personal" {
                        //TODO Add distance checks for difference between existing, non-null locations, and set locations
                        "viewer" {
                            playerAction {
                                player.toGeary().let {
                                    val wardrobe = it.get<PersonalWardrobe>()
                                    it.setPersisting(
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
                                player.toGeary().let {
                                    val wardrobe = it.get<PersonalWardrobe>()
                                    it.setPersisting(
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
                                player.toGeary().let {
                                    val wardrobe = it.get<PersonalWardrobe>()
                                    it.setPersisting(
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
                                val wardrobe = Wardrobe(
                                    player.uniqueId.toString(),
                                    it.wardrobeLocation,
                                    "mineinabyss.cosmetics.wardrobe",
                                    10
                                )
                                player.cosmeticUser?.enterWardrobe(false, wardrobe)
                            } ?: run {
                                val wardrobe =
                                    Wardrobe(
                                        player.uniqueId.toString(), PersonalWardrobe(player.location.clone(),
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
                                            }).wardrobeLocation, "mineinabyss.cosmetics.wardrobe", 10
                                    )
                                player.cosmeticUser?.enterWardrobe(false, wardrobe)
                            }

                        }
                    }*/
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
                        player.cosmeticUser?.let { user ->
                            user.getCosmetic(CosmeticSlot.valueOf(cosmeticSlot))?.let { cosmetic ->
                                DyeMenu.openMenu(user, cosmetic)
                            } ?: player.error("You do not have any cosmetic to dye")
                        }
                    }
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("cosmetics").filter { it.startsWith(args[0]) }
                2 -> when (args[0]) {
                    "cosmetics" -> listOf("menu", "wardrobe", "dye").filter { it.startsWith(args[1]) }
                    else -> listOf()
                }

                3 -> when (args[1]) {
                    //"wardrobe" -> listOf("personal", "open").filter { it.startsWith(args[2]) }
                    "dye" -> CosmeticSlot.entries.map { it.name }.filter { it.uppercase().startsWith(args[2]) }
                    else -> listOf()
                }

                4 -> when (args[2]) {
                    "personal" -> listOf("leave", "npc", "viewer").filter { it.startsWith(args[3]) }
                    else -> listOf()
                }

                else -> listOf()
            }
        }
    }
}
