package com.mineinabyss.features.cosmetics

import com.mineinabyss.components.cosmetics.CosmeticVoucher
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
import net.luckperms.api.node.Node
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class VendorListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.onRedeemCosmeticVoucher() {
        val (item, hand) = (item ?: return) to (hand ?: return)
        val voucher = player.inventory.toGeary()?.get(hand)?.get<CosmeticVoucher>() ?: return
        if (!rightClicked || player.hasPermission(voucher.permission)) return

        luckPerms.userManager.getUser(player.uniqueId)?.let {
            it.nodes += Node.builder(voucher.permission).build()
            luckPerms.userManager.saveUser(it)

            player.sendMessage("<gold>You have redeemed a Cosmetic Voucher for ${voucher.originalItem.displayName?.serialize()}!".miniMsg())
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            item.subtract()
        }
    }
}
