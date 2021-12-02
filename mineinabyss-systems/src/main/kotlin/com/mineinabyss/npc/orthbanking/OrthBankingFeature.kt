package com.mineinabyss.npc.orthbanking

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.isInHub
import com.mineinabyss.mineinabyss.updateBalance
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("orth_banking")
class OrthBankingFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(OrthBankingListener())

        commands {
            mineinabyss {
                "balance"(desc = "Toggles whether or not the balance should be shown.") {
                    playerAction {
                        val data = player.playerData

                        data.showPlayerBalance = !data.showPlayerBalance
                        data.showPlayerBalance.broadcastVal("balance: ")

                        if (data.showPlayerBalance) player.updateBalance()
                    }
                }
                "deposit"(desc = "Dev command until Guiy can take items") {
                    playerAction {
                        val data = player.playerData
                        val currItem = player.inventory.itemInMainHand
                        //val gearyEntity = currItem.toGearyOrNull(player)
                        //val orthCoin = gearyEntity?.get<OrthCoin>() ?: return@playerAction

                        if (!player.isInHub()) return@playerAction

                        data.orthCoinsHeld += 1
                        data.cloutTokensHeld += 1
                        //currItem.subtract(currItem.amount)
                        //currItem.subtract(currItem.amount).broadcastVal("amount: ")
                        if (data.showPlayerBalance) player.updateBalance()

                    }
                }
                "withdraw"(desc = "Dev command until Guiy can take items") {
                    playerAction {
                        val data = player.playerData
                        val slot = player.inventory.firstEmpty()

                        if (!player.isInHub()) return@playerAction

//                        if (slot == -1) {
//                            player.error("No empty slots in inventory")
//                            return@playerAction
//                        }

                        if (data.orthCoinsHeld > 0) data.orthCoinsHeld -= 1
                        if (data.cloutTokensHeld > 0) data.cloutTokensHeld -= 1
                        if (data.showPlayerBalance) player.updateBalance()
                    }
                }
            }
        }
    }
}