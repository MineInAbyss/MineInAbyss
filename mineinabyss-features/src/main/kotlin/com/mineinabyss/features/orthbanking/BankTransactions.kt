package com.mineinabyss.features.orthbanking

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.features.helpers.CoinFactory
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import org.bukkit.entity.Player

fun Player.depositCoins(amount: Int) {
    val player = player ?: return

    player.editPlayerData {
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
            orthCoinsHeld += amount
            item.subtract(amount)
            return@forEachIndexed
        }
    }
}

fun Player.withdrawCoins(amount: Int) = editPlayerData {
    val slot = inventory.firstEmpty()

    if (slot == -1) return@editPlayerData error("No empty slots in inventory")
    if (orthCoinsHeld == 0) return@editPlayerData error("Your account is empty...")
    if (orthCoinsHeld < amount) return@editPlayerData error("You don't have that many Orth Coins!")
    if (amount <= 0) return@editPlayerData error("You can't withdraw 0 coins!")

    val item = CoinFactory.orthCoin ?: kotlin.error("No orth coin prefab found")
    inventory.addItem(item.asQuantity(amount))
    orthCoinsHeld -= amount
    success("Your Orth Coins have been withdrawn!")
}
