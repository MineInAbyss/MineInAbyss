package com.mineinabyss.features.orthbanking

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.helpers.CoinFactory
import com.mineinabyss.features.hubstorage.isInHub
import com.mineinabyss.features.layers.LayersFeature
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.default
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
class OrthBankConfig {
    val enabled = false
    val balanceHudId: String = "balance_empty_offhand"
    val balanceHudOffhandId: String = "balance_offhand"
}

val OrthBankingFeature = module("orth-banking") {
    require(get<AbyssFeatureConfig>().orthBanking.enabled) { "Orth Banking feature is disabled" }
    singleModule(LayersFeature)
}.mainCommand {
    "bank" {
        description = "Orthbanking related commands"
        /*"balance"(description = "Toggles whether or not the balance should be shown.") {
            executes.asPlayer {
                val player = sender as Player
                player.info("Balance is now ${if (player.playerData.showPlayerBalance) "hidden" else "shown"}.")
                player.info("You have ${player.playerData.orthCoinsHeld} Orth Coins and ${player.playerData.mittyTokensHeld} Mitty Tokens.")

                player.playerData.showPlayerBalance = !player.playerData.showPlayerBalance
                when (player.playerData.showPlayerBalance) {
                    true -> player.success("Balance-HUD toggled on.")
                    false -> player.error("Balance-HUD toggled off.")
                }
            }
        }*/
        "deposit" {
            description = "Dev command until Guiy can take items"
            executes.asPlayer().args("amount" to Args.integer(min = 1).default { 1 }) { amount ->
                val player = sender as Player
                val currItem = player.inventory.itemInMainHand
                val gearyItem = player.inventory.toGeary()?.itemInMainHand

                when {
                    !player.isInHub() -> fail("You must be in Orth to make a deposit.")
                    gearyItem?.has<OrthCoin>() != true -> fail("You must be holding an Orth Coin to deposit.")
                    amount <= 0 -> fail("You can't deposit 0 Orth Coins.")
                    amount > currItem.amount -> fail("You don't have that many Orth Coins.")
                }

                currItem.subtract(amount)
                player.editPlayerData { orthCoinsHeld += amount }
            }
        }
        "withdraw" {
            description = "Dev command until Guiy can take items"
            executes.asPlayer().args("amount" to Args.integer(min = 1).default { 1 }) { amount ->
                var amount = amount
                val player = sender as? Player ?: return@args
                val slot = player.inventory.firstEmpty()
                val item = CoinFactory.orthCoin ?: fail("Failed to create Orth Coin.")
                val heldAmount = player.playerData.orthCoinsHeld

                when {
                    !player.isInHub() -> fail("You must be in Orth to make a withdraw.")
                    amount <= 0 -> fail("You can't withdraw 0 or less Orth Coins!")
                    amount > heldAmount -> fail("You don't have that many Orth Coins.")
                    slot == -1 -> fail("You do not have enough space in your inventory to withdraw the Orth Coins.")
                }

                amount = minOf(amount, 64)

                player.playerData.orthCoinsHeld = heldAmount - amount

                player.inventory.addItem(item.asQuantity(amount))
                player.success("You withdrew $amount Orth Coins from your balance.")
            }
        }
    }
}