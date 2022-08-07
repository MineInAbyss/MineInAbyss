package com.mineinabyss.npc.orthbanking

import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.helpers.updateBalance
import com.mineinabyss.hubstorage.isInHub
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.tracking.toGearyOrNull
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("orth_banking")
class OrthBankingFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(OrthBankingListener())

        commands {
            mineinabyss {
                "bank"(desc = "Orthbanking related commands"){
                    "balance"(desc = "Toggles whether or not the balance should be shown.") {
                        playerAction {
                            val player = sender as Player
                            val data = player.playerData
                            data.showPlayerBalance = !data.showPlayerBalance
                            if (data.showPlayerBalance) player.updateBalance()
                        }
                    }
                    "deposit"(desc = "Dev command until Guiy can take items") {
                        val amount by intArg { default = 1 }
                        playerAction {
                            val player = sender as Player
                            val data = player.playerData
                            val currItem = player.inventory.itemInMainHand

                            if (!player.isInHub()) {
                                player.error("You must be in Orth to make a deposit.")
                                return@playerAction
                            }
                            if (currItem.toGearyOrNull(player)?.has<OrthCoin>() != true) {
                                player.error("You must be holding an Orth Coin to deposit.")
                                return@playerAction
                            }

                            if (amount > player.inventory.itemInMainHand.amount) {
                                player.error("You don't have that many coins.")
                                return@playerAction
                            }

                            currItem.subtract(amount)
                            data.orthCoinsHeld += amount
                            if (data.showPlayerBalance) player.updateBalance()
                        }
                    }
                    "withdraw"(desc = "Dev command until Guiy can take items") {
                        var amount by intArg { default = 1 }
                        playerAction {
                            val player = sender as Player
                            val data = player.playerData
                            val slot = player.inventory.firstEmpty()

                            if (!player.isInHub()) return@playerAction
                            if (amount <= 0) {
                                player.error("You can't withdraw 0 or less coins!")
                                return@playerAction
                            }
                            if (amount > 64) amount = 64

                            if (slot == -1) {
                                player.error("You do not have enough space in your inventory to withdraw the coins.")
                                return@playerAction
                            }
                            val orthCoin = LootyFactory.createFromPrefab(PrefabKey.Companion.of("mineinabyss:orthcoin"))
                            if (orthCoin == null) {
                                player.error("Failed to create Orth Coin.")
                                return@playerAction
                            }

                            if (data.orthCoinsHeld > 0) data.orthCoinsHeld -= amount
                            if (data.showPlayerBalance) player.updateBalance()
                            player.inventory.addItem(orthCoin.asQuantity(amount))
                            player.success("You withdrew $amount Orth Coins from your guild.")
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "bank"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "bank" -> listOf("withdraw", "deposit", "balance")
                            else -> null
                        }
                    }
                    else -> null
                }
            }
        }
    }
}
