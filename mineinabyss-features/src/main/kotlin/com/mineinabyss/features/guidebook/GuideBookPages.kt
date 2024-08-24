package com.mineinabyss.features.guidebook

import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.geary.papermc.features.items.recipes.SetRecipes
import com.mineinabyss.geary.papermc.mythicmobs.spawning.SetMythicMob
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.idofront.entities.title
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.messaging.logInfo
import com.mineinabyss.idofront.messaging.logWarn
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.npc.ClientSideMerchant
import net.minecraft.world.inventory.MerchantMenu
import net.minecraft.world.item.trading.Merchant
import net.minecraft.world.item.trading.MerchantOffers
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.view.CraftMerchantView
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.MerchantView

abstract class GuideBookPage(player: Player, val title: Component) {
    private val serverPlayer = player.toNMS() as ServerPlayer
    private val clientMerchant = ClientSideMerchant(serverPlayer)
    val merchantMenu = MerchantMenu(serverPlayer.nextContainerCounter(), serverPlayer.inventory, clientMerchant)
    open val buttons = listOf<GuideBookButton>()
    open val backButton = GuideBookButton(TitleItem.of("<#E91E63>Back")) {
        GuideBookFrontPage(player).open()
    }

    fun open(title: Component? = this.title) {
        clientMerchant.openTradingScreen(serverPlayer, PaperAdventure.asVanilla(title), 0)
    }

    companion object {
        fun findGuideBookPage(player: Player): GuideBookPage? {
            val merchantView = player.openInventory as? CraftMerchantView ?: return null
            val merchant = GuideBookHelpers.MerchantMenuTrader.get(merchantView.handle) as Merchant
            val items = merchant.offers.map { it.itemCostA.itemStack.asBukkitCopy() }

            return GuideBookFrontPage(player).takeIf { page ->
                page.buttons.map { it.buttonItem }.let { buttons -> items.all { it in buttons } }
            } ?: GuideBookRecipesPage(player).takeIf { page ->
                page.buttons.map { it.buttonItem }.let { buttons -> items.all { it in buttons } }
            } ?: GuideBookBestiaryPage(player).takeIf { page ->
                page.buttons.map { it.buttonItem }.let { buttons -> items.all { it in buttons } }
            }
        }
    }
}

class GuideBookFrontPage(player: Player) : GuideBookPage(player, ":guidebook_front_page:".miniMsg()) {
    override val buttons = listOf(
        GuideBookButton(TitleItem.of("<red>Recipes")) {
            logError("Clicked recipes")
            GuideBookRecipesPage(player).open()
        },
        GuideBookButton(TitleItem.of("<yellow>Bestiary")) {
            logWarn("Clicked bestiary")
            GuideBookBestiaryPage(player).open()
        },
    )

    init {
        merchantMenu.offers = MerchantOffers().apply { addAll(buttons.map(GuideBookButton::merchantOffer)) }
    }
}

class GuideBookRecipesPage(player: Player) : GuideBookPage(player, ":guidebook_recipes_page:".miniMsg()) {
    override val buttons = backButton.plus(gearyItems.prefabs.map { it.key }
        .filter { it.toEntityOrNull()?.has<SetRecipes>() == true }
        .mapNotNull { gearyItems.createItem(it)?.editItemMeta { itemName(it.full.miniMsg()) } }
        .map { item -> GuideBookButton(item) { it.title(item.itemMeta.itemName()) } }
    )

    init {
        merchantMenu.offers = MerchantOffers().apply { addAll(buttons.map(GuideBookButton::merchantOffer)) }
    }
}

class GuideBookBestiaryPage(player: Player) : GuideBookPage(player, ":guidebook_bestiary_page:".miniMsg()) {
    override val buttons = backButton.plus(gearyMobs.query.prefabs.map { it.comp1 }
        .filter { it.toEntityOrNull()?.has<SetMythicMob>() == true }
        .map { ItemStack.of(Material.PAPER).editItemMeta { itemName(it.full.miniMsg()) } }
        .map { item -> GuideBookButton(item) { it.title(item.itemMeta.itemName()) } })

    init {
        merchantMenu.offers = MerchantOffers().apply { addAll(buttons.map(GuideBookButton::merchantOffer)) }
    }
}