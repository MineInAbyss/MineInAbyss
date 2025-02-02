package com.mineinabyss.features.orthbanking

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.features.helpers.CoinFactory
import com.mineinabyss.features.hubstorage.isInHub
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

class OrthBankingFeature(val config: Config) : Feature() {
    @Serializable
    class Config {
        val enabled = false
        val balanceHudId: String = "balance_empty_offhand"
        val balanceHudOffhandId: String = "balance_offhand"
    }

    override fun FeatureDSL.enable() {
        //plugin.listeners(OrthBankingListener())

        mainCommand {
            "bank"(desc = "Orthbanking related commands") {
                /*"balance"(desc = "Toggles whether or not the balance should be shown.") {
                    playerAction {
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
                "deposit"(desc = "Dev command until Guiy can take items") {
                    val amount by intArg { default = 1 }
                    ensureSenderIsPlayer()
                    action {
                        val player = sender as Player
                        val currItem = player.inventory.itemInMainHand
                        val gearyItem = player.inventory.toGeary()?.itemInMainHand

                        when {
                            !player.isInHub() -> return@action player.error("You must be in Orth to make a deposit.")
                            gearyItem?.has<OrthCoin>() != true -> return@action player.error("You must be holding an Orth Coin to deposit.")
                            amount <= 0 -> return@action player.error("You can't deposit 0 Orth Coins.")
                            amount > currItem.amount -> return@action player.error("You don't have that many Orth Coins.")
                        }

                        currItem.subtract(amount)
                        player.editPlayerData { orthCoinsHeld += amount }
                    }
                }
                "withdraw"(desc = "Dev command until Guiy can take items") {
                    var amount by intArg { default = 1 }
                    playerAction {
                        val player = sender as? Player ?: return@playerAction
                        val slot = player.inventory.firstEmpty()
                        val item = CoinFactory.orthCoin ?: return@playerAction player.error("Failed to create Orth Coin.")
                        val heldAmount = player.playerData.orthCoinsHeld

                        when {
                            !player.isInHub() -> return@playerAction player.error("You must be in Orth to make a withdraw.")
                            amount <= 0 -> return@playerAction player.error("You can't withdraw 0 or less Orth Coins!")
                            amount > heldAmount -> return@playerAction player.error("You don't have that many Orth Coins.")
                            slot == -1 -> return@playerAction player.error("You do not have enough space in your inventory to withdraw the Orth Coins.")
                        }

                        amount = minOf(amount, 64)

                        player.playerData.orthCoinsHeld = heldAmount - amount

                        player.inventory.addItem(item.asQuantity(amount))
                        player.success("You withdrew $amount Orth Coins from your balance.")
                    }
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("bank").filter { it.startsWith(args[0]) }
                2 -> when (args[0]) {
                    "bank" -> listOf("withdraw", "deposit"/*, "balance"*/)
                    else -> listOf()
                }.filter { it.startsWith(args[1]) }

                else -> listOf()
            }
        }
    }
}
