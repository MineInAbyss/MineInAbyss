package com.mineinabyss.npc.orthbanking

import androidx.compose.runtime.Composable
import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.nodes.InventoryCanvasScope.at
import com.mineinabyss.helpers.updateBalance
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventoryContext
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.BankMenu(player: Player) {
    Chest(listOf(player), "${Space.of(-18)}${ChatColor.WHITE}:orthbanking_menu:",
        4, onClose = {
            exit()
            player.updateBalance()
        }) {
        DepositCurrencyOption(player, Modifier.at(1, 1))
        WithdrawCurrencyOption(player, Modifier.at(5, 1))
    }
}

@Composable
fun DepositCurrencyOption(player: Player, modifier: Modifier) {
    val data = player.playerData
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guiy { DepositCurrencyMenu(player) }
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Open Deposit Menu")
                lore =
                    listOf("${ChatColor.YELLOW}You currently have ${ChatColor.ITALIC}${data.orthCoinsHeld} ${ChatColor.YELLOW}coins in your account.")
            })
        }
    }
}

@Composable
fun GuiyOwner.DepositCurrencyMenu(player: Player) {
    Chest(listOf(player), "${Space.of(-18)}${ChatColor.WHITE}:orthbanker_deposit_menu:",
        5, onClose = {
            exit()
            player.updateBalance()
        })
    {
        Deposit(player, modifier = Modifier)
    }
}

@Composable
fun Deposit(player: Player, modifier: Modifier) {
    var amount = 1

    Grid(3, 2, modifier.at(3, 0).clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        amount += 1
        if (amount > 64) amount = 64
        broadcast(amount)
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Increase Deposit")
            })
        }
    }

    Grid(1, 1, modifier.at(4, 2).clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        broadcast(amount)
        player.onDepositCoins(amount)
        player.updateBalance()
        player.closeInventory()
    })
    {
        repeat(1) {
            Item(ItemStack(Material.EMERALD).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Confirm Deposit")
            })
        }
    }

    Grid(3, 1, modifier.at(3, 3).clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        amount -= 1
        if (amount < 1) amount = 1
        broadcast(amount)
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Decrease Deposit")
            })
        }
    }

}

fun Player.onDepositCoins(amount: Int) {
    val player = player ?: return
    val data = player.playerData

    player.inventory.contents.forEach {
        if (it == null) {
            player.success("Your Orth Coins have been deposited!")
            return
        }
        val current = it.toGearyOrNull(player) ?: return@forEach
        current.get<OrthCoin>() ?: return@forEach

        if (it.amount < amount) {
            player.error("You don't have that many Orth Coins!")
            return
        }

        data.orthCoinsHeld += amount
        it.subtract(amount)
        return@forEach
    }

}

@Composable
fun WithdrawCurrencyOption(player: Player, modifier: Modifier) {
    val data = player.playerData
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guiy { WithdrawCurrencyMenu(player) }
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Open Withdrawal Menu")
                lore =
                    mutableListOf("${ChatColor.YELLOW}You currently have ${ChatColor.ITALIC}${data.orthCoinsHeld} ${ChatColor.YELLOW}coins in your account.")
            })
        }
    }
}

@Composable
fun GuiyOwner.WithdrawCurrencyMenu(player: Player) {
    Chest(listOf(player), "${Space.of(18)}${ChatColor.WHITE}:orthbanker_withdrawal_menu:",
        5, onClose = {
            exit()
            player.updateBalance()
        }) {
        Withdraw(player, modifier = Modifier)
    }
}

@Composable
fun Withdraw(player: Player, modifier: Modifier) {
    var amount = 1

    Grid(3, 2, modifier.at(3, 0).clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        amount += 1
        if (amount > 64) amount = 64
        broadcast(amount)
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Increase Withdrawal")
            })
        }
    }

    Grid(1, 1, modifier.at(4, 2).clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        broadcast(amount)
        player.onWithdrawCoins(amount)
        player.updateBalance()
        player.closeInventory()
    })
    {
        repeat(1) {
            Item(ItemStack(Material.EMERALD).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Confirm Withdrawal")
            })
        }
    }

    Grid(3, 1, modifier.at(3, 3).clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        amount -= 1
        if (amount < 1) amount = 1
        broadcast(amount)
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Decrease Withdrawal")
            })
        }
    }

}

fun Player.onWithdrawCoins(amount: Int) {
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

    if (data.orthCoinsHeld < amount) {
        player.error("You don't have that many Orth Coins!")
        return
    }

    loop@ for (i in 1..amount) {
        LootyFactory.createFromPrefab(PrefabKey.of("mineinabyss:orthcoin"))?.let { player.inventory.addItem(it) }
        LootyFactory.loadFromPlayerInventory(PlayerInventoryContext(player, slot))
        data.orthCoinsHeld -= 1
    }
    player.success("Your Orth Coins have been withdrawn!")
}
