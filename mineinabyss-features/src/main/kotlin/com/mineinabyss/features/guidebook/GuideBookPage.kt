package com.mineinabyss.features.guidebook

import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.entities.title
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import net.minecraft.core.component.DataComponentPredicate
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.npc.ClientSideMerchant
import net.minecraft.world.inventory.MerchantMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers
import org.bukkit.entity.Player

@Serializable(GuideBookPages.Serializer::class)
data class GuideBookPages(val pages: List<GuideBookPage> = listOf()) {

    @Transient private val ids = pages.map { it.id }

    class Serializer : InnerSerializer<Map<String, GuideBookPage>, GuideBookPages>(
        "mineinabyss:guidepages",
        MapSerializer(String.serializer(), GuideBookPage.serializer()),
        { GuideBookPages(it.map { it.value.copy(id = it.key) }) },
        { it.pages.associateBy { it.id } }
    )
}

@Serializable
data class GuideBookPage(
    // This is blank by default to avoid marking it as null
    // The Serializer in PackyTemplates will always ensure the id is properly set
    @Transient val id: String = "",
    val title: String,
    val buttons: List<SerializableItemStack> = emptyList()
) {
    fun openMerchantMenu(player: Player) {
        //If no buttons, the page is only meant to change the title and doesnt have any sub-pages
        if (buttons.isEmpty()) player.openInventory.title(title.miniMsg())
        else {
            val serverPlayer = player.toNMS() as ServerPlayer
            val clientMerchant = ClientSideMerchant(serverPlayer)
            val merchantMenu = MerchantMenu(serverPlayer.nextContainerCounter(), serverPlayer.inventory, clientMerchant)
            merchantMenu.setShowProgressBar(false)
            val offers = MerchantOffers()
            buttons.map(GuideBookHelpers::MerchantOffer).let(offers::addAll)
            merchantMenu.offers = offers
            merchantMenu.setSelectionHint(0)

            clientMerchant.openTradingScreen(serverPlayer, PaperAdventure.asVanilla(title.miniMsg()), 0)
        }

    }
}
