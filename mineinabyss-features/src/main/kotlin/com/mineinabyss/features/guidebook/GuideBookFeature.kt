package com.mineinabyss.features.guidebook

import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.npc.ClientSideMerchant
import net.minecraft.world.inventory.MerchantMenu
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class GuideBookFeature : Feature() {

    override fun FeatureDSL.enable() {
        plugin.listeners(GuideBookListener())

        mainCommand {
            "guidebook" {
                playerAction {
                    GuideBookPage.FrontPage(player).open()
                }
            }
        }
    }
}