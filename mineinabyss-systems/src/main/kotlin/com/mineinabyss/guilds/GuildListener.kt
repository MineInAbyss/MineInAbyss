package com.mineinabyss.guilds

import com.mineinabyss.components.guilds.GuildMaster
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import com.mineinabyss.guilds.menus.GuildMainMenu
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.mineinabyss.mineinabyss.data.GuildRanks
import com.mineinabyss.mineinabyss.data.MessageQueue
import com.mineinabyss.mineinabyss.data.MessageQueue.content
import com.mineinabyss.mineinabyss.extensions.getGuildName
import com.mineinabyss.mineinabyss.extensions.getGuildRank
import com.mineinabyss.mineinabyss.extensions.hasGuild
import com.okkero.skedule.schedule
import nl.rutgerkok.blocklocker.group.GroupSystem
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GuildListener : Listener {

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractGuildMaster() {
        val entity = rightClicked.toGearyOrNull() ?: return
        entity.get<GuildMaster>() ?: return

        guiy { GuildMainMenu(player) }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        mineInAbyss.schedule {
            waitFor(20)
            transaction {
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
