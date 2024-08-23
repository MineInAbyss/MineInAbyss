package com.mineinabyss.features.guidebook

import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.serialization.Serializable
import net.minecraft.core.component.DataComponentPredicate
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.npc.ClientSideMerchant
import net.minecraft.world.inventory.MerchantMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers
import org.bukkit.Material
import java.util.*

class GuideBookFeature(val config: Config) : FeatureWithContext<GuideBookFeature.Context>(::Context) {
    class Context : Configurable<GuideBookConfig> {
        override val configManager = config("guideBook", abyss.dataPath, GuideBookConfig(""))
    }

    @Serializable
    class Config(
        val enabled: Boolean = true,
        val frontPage: String = "",
    )

    override fun FeatureDSL.enable() {
        plugin.listeners(GuideBookListener())

        mainCommand {
            "guidebook" {
                playerAction {
                    val serverPlayer = player.toNMS() as? ServerPlayer ?: return@playerAction
                    val clientMerchant = ClientSideMerchant(serverPlayer)
                    val merchantMenu = MerchantMenu(serverPlayer.nextContainerCounter(), serverPlayer.inventory, clientMerchant)

                    merchantMenu.title = PaperAdventure.asVanilla("<rainbow>test123321test:pog:".miniMsg())
                    merchantMenu.offers = MerchantOffers().also { offers ->
                        val transparentItem = ItemStack.fromBukkitCopy(TitleItem.transparentItem)
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
            }
        }
    }
}