package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.gui.Menus
import com.hibiscusmc.hmccosmetics.gui.type.Types
import com.mineinabyss.features.AbyssContext
import com.mineinabyss.features.helpers.cosmeticUser
import com.mineinabyss.features.helpers.hmcCosmetics
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.plugin.listeners
import kotlinx.serialization.Serializable

@Serializable
class CosmeticsConfig {
    val enabled = false
    val equipWhistleCosmetic = false
}
val CosmeticsFeature = feature("cosmetics") {
    dependsOn {
        plugins("HMCCosmetics")
    }
    onEnable {
        val config = get<AbyssContext>().config.cosmetics
        Types.addType(TypeMiaCosmetic())
        HMCCosmeticsPlugin.setup()
        Cosmetics.addCosmetic(EmptyBackpackCosmetic)
        listeners(CosmeticListener(config))

        // TODO implement command
//        mainCommand {
//            "cosmetics" {
//                "menu" {
//                    playerAction {
//                        if (hmcCosmetics.isEnabled) Menus.getDefaultMenu()?.openMenu(player.cosmeticUser)
//                    }
//                }
//            }
//        }
//        tabCompletion {
//            when (args.size) {
//                1 -> listOf("cosmetics").filter { it.startsWith(args[0]) }
//                2 -> when (args[0]) {
//                    "cosmetics" -> listOf("menu", "wardrobe", "dye").filter { it.startsWith(args[1]) }
//                    else -> listOf()
//                }
//
//                3 -> when (args[1]) {
//                    //"wardrobe" -> listOf("personal", "open").filter { it.startsWith(args[2]) }
//                    "dye" -> CosmeticSlot.values().keys.filter { it.uppercase().startsWith(args[2]) }
//                    else -> listOf()
//                }
//
//                4 -> when (args[2]) {
//                    "personal" -> listOf("leave", "npc", "viewer").filter { it.startsWith(args[3]) }
//                    else -> listOf()
//                }
//
//                else -> listOf()
//            }
//        }
    }
}
