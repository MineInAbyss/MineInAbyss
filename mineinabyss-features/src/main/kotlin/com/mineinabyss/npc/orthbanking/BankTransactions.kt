package com.mineinabyss.npc.orthbanking

import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.entity.Player

fun Player.depositCoins(amount: Int) {
    val player = player ?: return
    val data = player.playerData

    player.inventory.contents?.forEach {
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

    val item =
        LootyFactory.createFromPrefab(PrefabKey.of("mineinabyss:orthcoin")) ?: kotlin.error("No orth coin prefab found")
    for (i in 1..amount) {
        inventory.addItem(item)
        data.orthCoinsHeld -= 1
    }
    success("Your Orth Coins have been withdrawn!")
}
