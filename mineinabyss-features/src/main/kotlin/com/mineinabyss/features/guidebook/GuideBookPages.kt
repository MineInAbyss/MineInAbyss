package com.mineinabyss.features.guidebook

import com.mineinabyss.features.abyss
import com.mineinabyss.features.guidebook.GuideBookHelpers.toMerchantOffers
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.di.Features.layers
import com.mineinabyss.geary.papermc.features.items.recipes.SetRecipes
import com.mineinabyss.geary.papermc.mythicmobs.spawning.SetMythicMob
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.idofront.entities.title
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.core.NonNullList
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.npc.ClientSideMerchant
import net.minecraft.world.inventory.MerchantMenu
import net.minecraft.world.item.trading.Merchant
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.view.CraftMerchantView
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class GuideBookPage(val player: Player, val title: Component) {
    constructor(player: Player, title: String) : this(player, title.miniMsg())

    private val serverPlayer = player.toNMS() as ServerPlayer
    private val clientMerchant = ClientSideMerchant(serverPlayer)

    open val buttons = listOf<GuideBookButton>()
    open val backButton = GuideBookButton(TitleItem.of("<#E91E63>Back")) { FrontPage(player).open() }
    open val merchantMenu = MerchantMenu(serverPlayer.nextContainerCounter(), serverPlayer.inventory, clientMerchant).apply {
        setCanRestock(false)
    }

    fun open(title: Component? = this.title) {
        clientMerchant.openTradingScreen(serverPlayer, PaperAdventure.asVanilla(title), 0)
        GuideBookHelpers.hideInventory(player)
    }

    companion object {
        fun findGuideBookPage(player: Player): GuideBookPage? {
            val merchantView = player.openInventory as? CraftMerchantView ?: return null
            val merchant = GuideBookHelpers.MerchantMenuTrader.get(merchantView.handle) as Merchant
            val items = merchant.offers.map { it.itemCostA.itemStack.asBukkitCopy() }

            return FrontPage(player).takeIf { it.isPage(items) }
                ?: RecipesPage(player).takeIf { it.isPage(items) }
                ?: BestiaryPage(player).takeIf { it.isPage(items) }
                ?: LayersPage(player).takeIf { it.isPage(items) }
        }

        private fun GuideBookPage.isPage(merchantOffers: List<ItemStack>): Boolean {
            val buttonItems = buttons.map(GuideBookButton::buttonItem)
            return merchantOffers.all { it in buttonItems }
        }
    }

    class FrontPage(player: Player) : GuideBookPage(player, ":guidebook_front_page:") {
        override val buttons = listOf(
            GuideBookButton(ItemStack.of(Material.CRAFTING_TABLE).editItemMeta { itemName("<red>Recipes".miniMsg()) }) { RecipesPage(player).open() },
            GuideBookButton(ItemStack.of(Material.BOOK).editItemMeta { setCustomModelData(1);itemName("<yellow>Bestiary".miniMsg()) }) { BestiaryPage(player).open() },
            GuideBookButton(ItemStack.of(Material.ENCHANTED_BOOK).editItemMeta { setEnchantmentGlintOverride(false); setCustomModelData(1); itemName("<aqua>Layers".miniMsg()) }) { LayersPage(player).open() },
        )

        override val merchantMenu = super.merchantMenu.apply { offers = buttons.toMerchantOffers() }
    }

    class RecipesPage(player: Player) : GuideBookPage(player, ":guidebook_recipes_page:") {
        override val buttons = backButton.plus(gearyItems.prefabs.map { it.key }
            .filter { it.toEntityOrNull()?.has<SetRecipes>() == true }
            .mapNotNull { gearyItems.createItem(it)?.editItemMeta { itemName(it.full.miniMsg()) } }
            .map { item -> GuideBookButton(item) { it.title(item.itemMeta.itemName()) } }
        )

        override val merchantMenu = super.merchantMenu.apply { offers = buttons.toMerchantOffers() }
    }

    class BestiaryPage(player: Player) : GuideBookPage(player, ":guidebook_bestiary_page:") {
        override val buttons = backButton.plus(gearyMobs.query.prefabs.map { it.comp1 }
            .filter { it.toEntityOrNull()?.has<SetMythicMob>() == true }
            .map { ItemStack.of(Material.PAPER).editItemMeta { itemName(it.full.miniMsg()) } }
            .map { item -> GuideBookButton(item) { it.title(item.itemMeta.itemName()) } })

        override val merchantMenu = super.merchantMenu.apply { offers = buttons.toMerchantOffers() }
    }

    class LayersPage(player: Player) : GuideBookPage(player, ":guidebook_layers_page:".miniMsg()) {
        override val buttons = backButton.plus(layers.config.layers
            .map { TitleItem.of(it.name) }
            .map { item -> GuideBookButton(item) { it.title(item.itemMeta.itemName()) } })

        override val merchantMenu = super.merchantMenu.apply { offers = buttons.toMerchantOffers() }
    }
}