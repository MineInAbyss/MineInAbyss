package com.mineinabyss.npc.orthbanking

import androidx.compose.runtime.Composable
import com.mineinabyss.components.npc.OrthBanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventoryContext
import com.mineinabyss.looty.tracking.toGearyOrNull
import com.mineinabyss.mineinabyss.updateBalance
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.BankMenu(player: Player) {
    Chest(listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:orthbanker_menu:",
        4, onClose = {
            exit()
            player.updateBalance()
        }) {
        DepositCurrency(player, Modifier.at(1, 1))
        WithdrawCurrency(player, Modifier.at(5, 1))
    }
}

@Composable
fun DepositCurrency(player: Player, modifier: Modifier) {
    val data = player.playerData
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.onDepositCoins()
        player.updateBalance()
        player.closeInventory()
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Deposit Orth Coins")
                lore = mutableListOf("${ChatColor.YELLOW}You currently have ${ChatColor.ITALIC}${data.orthCoinsHeld} ${ChatColor.YELLOW}coins in your account.")
            })
        }
    }
}

@Composable
fun WithdrawCurrency(player: Player, modifier: Modifier) {
    val data = player.playerData
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 0.1f)
        player.onWithdrawCoins()
        player.updateBalance()

        player.closeInventory()
    }) {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Withdraw Orth Coins")
                lore = mutableListOf("${ChatColor.YELLOW}You currently have ${ChatColor.ITALIC}${data.orthCoinsHeld} ${ChatColor.YELLOW}coins in your account.")
            })
        }
    }
}

fun Player.onDepositCoins() {
    val player = player ?: return
    val data = player.playerData

    player.inventory.contents.forEach {
        if (it == null) {
            player.success("Your Orth Coins have been deposited!")
            return
        }
        val current = it.toGearyOrNull(player) ?: return@forEach
        current.get<OrthCoin>() ?: return@forEach

        it.amount.broadcastVal("coins: ")
        data.orthCoinsHeld += it.amount
        it.subtract(it.amount)
        data.orthCoinsHeld.broadcastVal("amount: ")
        return@forEach
    }

}

fun Player.onWithdrawCoins() {
    val player = player ?: return
    val data = player.playerData
    val slot = player.inventory.firstEmpty()

    if (slot == -1) {
        player.error("No empty slots in inventory")
        return
    }

    if (data.orthCoinsHeld == 0) {
        player.error("Your account is empty...")
        return
    }

    loop@ for (i in 1..data.orthCoinsHeld){
        if (i == 65) break@loop
        LootyFactory.createFromPrefab(PrefabKey.of("mineinabyss:orthcoin"))?.let { player.inventory.addItem(it) }
        LootyFactory.loadFromPlayerInventory(PlayerInventoryContext(player, slot))
        data.orthCoinsHeld -= 1
    }

    player.error("Your Orth Coins have been withdrawn!")
}