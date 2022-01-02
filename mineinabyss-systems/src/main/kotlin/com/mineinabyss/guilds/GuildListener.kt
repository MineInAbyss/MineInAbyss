package com.mineinabyss.guilds

import com.mineinabyss.components.guilds.GuildMaster
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import com.mineinabyss.guilds.menus.GuildMainMenu
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.mineinabyss.mineinabyss.data.GuildRanks
import com.mineinabyss.mineinabyss.data.MessageQueue
import com.mineinabyss.mineinabyss.data.MessageQueue.content
import com.mineinabyss.mineinabyss.extensions.getGuildName
import com.mineinabyss.mineinabyss.extensions.getGuildRank
import com.mineinabyss.mineinabyss.extensions.hasGuild
import com.okkero.skedule.schedule
import nl.rutgerkok.blocklocker.group.GroupSystem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class GuildListener : Listener {
    //TODO move this cooldown into geary commons
    val clickedCooldown = mutableMapOf<UUID, Int>()
    @EventHandler
    fun PlayerInteractEntityEvent.onInteractGuildMaster() {
        val entity = rightClicked.toGearyOrNull() ?: return
        entity.get<GuildMaster>() ?: return

        if((clickedCooldown[player.uniqueId] ?: 0) < Bukkit.getCurrentTick()) {
            clickedCooldown[player.uniqueId] = Bukkit.getCurrentTick() + 5
            guiy { GuildMainMenu(player) }
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        mineInAbyss.schedule {
            waitFor(20)
            transaction(AbyssContext.db) {
                MessageQueue.select { MessageQueue.playerUUID eq player.uniqueId }.forEach {
                    player.sendMessage(it[content])
                }
                MessageQueue.deleteWhere { MessageQueue.playerUUID eq player.uniqueId }
            }
        }
    }
}

class GuildContainerSystem : GroupSystem() {

    override fun isInGroup(player: Player, guildName: String): Boolean {
        val guild = player.hasGuild()
        if (!guild) return false

        val name = player.getGuildName()
        return name.equals(guildName, true)
    }

    override fun isGroupLeader(player: Player, groupName: String): Boolean {
        val guild = player.hasGuild()
        if (!guild) return false

        return player.getGuildRank() == GuildRanks.Owner
    }

}

class GuildChatSystem : Listener {

    @EventHandler
    fun AsyncPlayerChatEvent.toggleGuildChat() {
        val allPlayers = Bukkit.getOnlinePlayers()

        recipients.clear()
        recipients.add(player)

        if (player.playerData.guildChatStatus) {
            format = ":survival::guildchat: ${player.displayName}: $message"
            allPlayers.forEach {
                if (it.getGuildName().lowercase() != player.getGuildName().lowercase()) recipients.remove(it)
                else recipients.add(it)
            }
        }
        else {
            allPlayers.forEach {
                recipients.add(it)
            }
        }
    }
}
