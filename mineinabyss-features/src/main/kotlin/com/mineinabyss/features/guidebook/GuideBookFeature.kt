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
                    GuideBookFrontPage(player).open()
                }
            }
            /*"guidebook_old" {
                playerAction {
                    val serverPlayer = player.toNMS() as? ServerPlayer ?: return@playerAction
                    val clientMerchant = ClientSideMerchant(serverPlayer)
                    val merchantMenu = MerchantMenu(serverPlayer.nextContainerCounter(), serverPlayer.inventory, clientMerchant)

                    merchantMenu.title = PaperAdventure.asVanilla("<rainbow>test123321test:pog:".miniMsg())
                    merchantMenu.offers = MerchantOffers().also { offers ->
                        val transparentItem = ItemStack.fromBukkitCopy(TitleItem.transparentItem)
                        val currentPageItem = ItemStack.fromBukkitCopy(TitleItem.transparentItem.editItemMeta { itemName("".miniMsg()) })
                        transparentItem.set(DataComponents.ITEM_NAME, Component.literal("Back").withColor(15277667))
                        offers.add(MerchantOffer(ItemCost(transparentItem.itemHolder, 1, DataComponentPredicate.allOf(transparentItem.components)), transparentItem, 0, 0, 0f))
                        for (x in 1..6) {
                            transparentItem.set(DataComponents.ITEM_NAME, Component.literal(x.toString()))
                            offers.add(MerchantOffer(ItemCost(transparentItem.itemHolder, 1, DataComponentPredicate.allOf(transparentItem.components)), transparentItem, 0, 0, 0f))
                        }
                    }

                    merchantMenu.setShowProgressBar(false)
                    clientMerchant.openTradingScreen(serverPlayer, Component.literal(":guidebook_page1:"), 0)
                }
            }*/
        }
    }
}