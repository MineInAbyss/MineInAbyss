package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.gui.Menus
import com.hibiscusmc.hmccosmetics.gui.type.Types
import com.mineinabyss.features.helpers.cosmeticUser
import com.mineinabyss.features.helpers.hmcCosmetics
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners
import kotlinx.serialization.Serializable

class CosmeticsFeature(val config: Config) : Feature() {

    @Serializable
    class Config {
        val enabled = false
        val equipWhistleCosmetic = false
    }

    override val dependsOn: Set<String> = setOf("HMCCosmetics")
    override fun FeatureDSL.enable() {
        Types.addType(TypeMiaCosmetic())
        HMCCosmeticsPlugin.setup()
        Cosmetics.addCosmetic(EmptyBackpackCosmetic)
        plugin.listeners(CosmeticListener(config.equipWhistleCosmetic))

        mainCommand {
            "cosmetics" {
                "menu" {
                    playerAction {
                        if (hmcCosmetics.isEnabled) Menus.getDefaultMenu()?.openMenu(player.cosmeticUser)
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
                    "dye" -> CosmeticSlot.values().keys.filter { it.uppercase().startsWith(args[2]) }
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
