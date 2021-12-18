package com.mineinabyss.npc.orthbanking

import androidx.compose.runtime.Composable
import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.nodes.InventoryCanvasScope.at
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.updateBalance
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventoryContext
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.ChatColor.*
import org.bukkit.Sound
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.BankMenu(player: Player) {
    Chest(listOf(player), "${Space.of(-18)}$WHITE:orthbanking_menu:",
        4, onClose = {
            player.updateBalance()
            exit()
        }) {
        DepositCurrencyOption(player, Modifier.at(1, 1))
        WithdrawCurrencyOption(player, Modifier.at(5, 1))
    }
}

@Composable
fun DepositCurrencyOption(player: Player, modifier: Modifier) {
    val data = player.playerData
    Item(
        TitleItem.of(
            "$GOLD${BOLD}Open Deposit Menu",
            "${YELLOW}You currently have $ITALIC${data.orthCoinsHeld} ${YELLOW}coins in your account."
        ),
        modifier.size(3, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            guiy { DepositCurrencyMenu(player) }
        }
    )
}

@Composable
fun GuiyOwner.DepositCurrencyMenu(player: Player) {
    Chest(listOf(player), "${Space.of(-18)}$WHITE:orthbanker_deposit_menu:", height = 5,
        onClose = {
            player.updateBalance()
            exit()
        }
    ) {
        Deposit(player, modifier = Modifier)
    }
}

@Composable
fun Deposit(player: Player, modifier: Modifier) {
    var amount = 1

    Item(
        TitleItem.of("$GOLD${BOLD}Increase Deposit"),
        modifier.size(3, 2).at(3, 0).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            amount += 1
            if (amount > 64) amount = 64
            broadcast(amount)
        }
    )

    Item(
        TitleItem.of("$GOLD${BOLD}Confirm Deposit"),
        modifier.at(4, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            broadcast(amount)
            player.onDepositCoins(amount)
            player.updateBalance()
            player.closeInventory()
        }
    )

    Item(
        TitleItem.of("$GOLD${BOLD}Decrease Deposit"),
        modifier.size(3, 1).at(3, 3).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            amount -= 1
            if (amount < 1) amount = 1
            broadcast(amount)
        }
    )
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
    Item(
        TitleItem.of(
            "$GOLD${BOLD}Open Withdrawal Menu",
            "${YELLOW}You currently have $ITALIC${data.orthCoinsHeld} ${YELLOW}coins in your account."
        ),
        modifier.size(3, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            guiy { WithdrawCurrencyMenu(player) }
        }
    )
}

@Composable
fun GuiyOwner.WithdrawCurrencyMenu(player: Player) {
    Chest(listOf(player), "${Space.of(18)}$WHITE:orthbanker_withdrawal_menu:",
        5, onClose = {
            player.updateBalance()
            exit()
        }) {
        Withdraw(player, modifier = Modifier)
    }
}

@Composable
fun Withdraw(player: Player, modifier: Modifier) {
    var amount = 1

    Item(
        TitleItem.of("$GOLD${BOLD}Increase Withdrawal"),
        modifier.size(3, 2).at(3, 0).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            amount += 1
            if (amount > 64) amount = 64
            broadcast(amount)
        }
    )

    Item(
        TitleItem.of("$GOLD${BOLD}Confirm Withdrawal"),
        modifier.at(4, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            broadcast(amount)
            player.onWithdrawCoins(amount)
            player.updateBalance()
            player.closeInventory()
        }
    )

    Item(
        TitleItem.of("$GOLD${BOLD}Decrease Withdrawal"),
        modifier.size(3, 1).at(3, 3).clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        amount -= 1
        if (amount < 1) amount = 1
        broadcast(amount)
    })

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
