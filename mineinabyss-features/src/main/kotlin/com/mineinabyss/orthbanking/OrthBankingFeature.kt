package com.mineinabyss.orthbanking

import com.mineinabyss.components.npc.orthbanking.MittyToken
import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.helpers.createMittyToken
import com.mineinabyss.helpers.createOrthCoin
import com.mineinabyss.helpers.toggleHud
import com.mineinabyss.hubstorage.isInHub
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.ensureSenderIsPlayer
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.looty.tracking.toGearyOrNull
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("orth_banking")
class OrthBankingFeature(
    val balanceHudId: String = "balance"
) : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(OrthBankingListener(this@OrthBankingFeature))

        commands {
            mineinabyss {
                "bank"(desc = "Orthbanking related commands") {
                    "balance"(desc = "Toggles whether or not the balance should be shown.") {
                        ensureSenderIsPlayer()
                        action {
                            val player = sender as Player
                            player.playerData.showPlayerBalance = !player.playerData.showPlayerBalance
                            (sender as Player).toggleHud(balanceHudId, player.playerData.showPlayerBalance)
                        }
                    }
                    "deposit"(desc = "Dev command until Guiy can take items") {
                        val amount by intArg { default = 1 }
                        ensureSenderIsPlayer()
                        action {
                            val player = sender as Player
                            val currItem = player.inventory.itemInMainHand
                            val gearyItem = currItem.toGearyOrNull(player)
                            val isOrthCoin = currItem.isSimilar(createOrthCoin())
                            val currency =
                                if (isOrthCoin) "coins" else "tokens"

                            if (!player.isInHub()) {
                                player.error("You must be in Orth to make a deposit.")
                                return@action
                            }

                            if (gearyItem?.has<OrthCoin>() != true && gearyItem?.has<MittyToken>() != true) {
                                player.error("You must be holding an Orth Coin or a Mitty Token to deposit.")
                                return@action
                            }

                            if (amount <= 0) {
                                player.error("You can't deposit 0 $currency.")
                                return@action
                            }

                            if (amount > currItem.amount) {
                                player.error("You don't have that many $currency.")
                                return@action
                            }

                            currItem.subtract(amount)
                            if (isOrthCoin) player.playerData.orthCoinsHeld else player.playerData.mittyTokensHeld += amount
                        }
                    }
                    "withdraw"(desc = "Dev command until Guiy can take items") {
                        val type by optionArg(listOf("orthcoin", "mittytoken")) { default = "orthcoin" }
                        var amount by intArg { default = 1 }
                        playerAction {
                            val player = sender as? Player ?: return@playerAction
                            val slot = player.inventory.firstEmpty()
                            val isOrthCoin = type == "orthcoin"
                            val item = (if (isOrthCoin) createOrthCoin() else createMittyToken())
                            val currency =
                                if (isOrthCoin) "coins" else if (type != "mitytoken") "tokens" else return@playerAction
                            val heldAmount =
                                if (isOrthCoin) player.playerData.orthCoinsHeld else player.playerData.mittyTokensHeld

                            if (!player.isInHub()) {
                                player.error("You must be in Orth to make a withdraw.")
                                return@playerAction
                            }

                            if (amount <= 0) {
                                player.error("You can't withdraw 0 or less $currency!")
                                return@playerAction
                            }

                            if (amount > heldAmount) {
                                player.error("You don't have that many $currency.")
                                return@playerAction
                            }

                            if (amount > 64) amount = 64

                            if (slot == -1) {
                                player.error("You do not have enough space in your inventory to withdraw the $currency.")
                                return@playerAction
                            }

                            if (item == null) {
                                player.error("Failed to create $currency.")
                                return@playerAction
                            }

                            if (isOrthCoin) player.playerData.orthCoinsHeld else player.playerData.mittyTokensHeld -= amount
                            player.inventory.addItem(item.asQuantity(amount))
                            player.success("You withdrew $amount $currency from your balance.")
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("bank").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "bank" -> listOf("withdraw", "deposit", "balance").filter { it.startsWith(args[1]) }
                            else -> null
                        }
                    }

                    else -> null
                }
            }
        }
    }
}
