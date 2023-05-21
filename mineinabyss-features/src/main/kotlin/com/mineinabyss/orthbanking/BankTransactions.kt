package com.mineinabyss.orthbanking

import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.tracking.items.toGeary
import com.mineinabyss.helpers.CoinFactory
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import org.bukkit.entity.Player

fun Player.depositCoins(amount: Int) {
    val player = player ?: return
    val data = player.playerData

    player.inventory.contents.forEachIndexed { index, item ->
        if (item == null) {
            player.success("Your Orth Coins have been deposited!")
            return
        }
        player.inventory.toGeary()?.get(index)?.get<OrthCoin>() ?: return@forEachIndexed

        if (item.amount < amount) {
            player.error("You don't have that many Orth Coins!")
            return
        }

        data.orthCoinsHeld += amount
        item.subtract(amount)
        return@forEachIndexed
    }
}

fun Player.withdrawCoins(amount: Int) {
    val data = playerData
    val slot = inventory.firstEmpty()

    if (slot == -1) {
        error("No empty slots in inventory")
        return
    }

    if (data.orthCoinsHeld == 0) {
        error("Your account is empty...")
        return
    }

    if (data.orthCoinsHeld < amount) {
        error("You don't have that many Orth Coins!")
        return
    }

    if (amount <= 0) {
        error("You can't withdraw 0 coins!")
        return
    }

    val item = CoinFactory.orthCoin ?: kotlin.error("No orth coin prefab found")
    for (i in 1..amount) {
        inventory.addItem(item)
        data.orthCoinsHeld -= 1
    }
    success("Your Orth Coins have been withdrawn!")
}
