package com.mineinabyss.guilds

import com.mineinabyss.components.guilds.GuildMaster
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.guilds.database.GuildRanks
import com.mineinabyss.guilds.menus.GuildMainMenu
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.helpers.MessageQueue
import com.mineinabyss.helpers.MessageQueue.content
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.mineinabyss.mineinabyss.extensions.getGuildName
import com.mineinabyss.mineinabyss.extensions.getGuildRank
import com.mineinabyss.mineinabyss.extensions.hasGuild
import com.okkero.skedule.schedule
import nl.rutgerkok.blocklocker.group.GroupSystem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GuildListener : Listener {
    //TODO move this cooldown into geary commons
    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteractGuildMaster(feature: GuildFeature) {
        val entity = rightClicked.toGearyOrNull() ?: return
        if (!entity.has<GuildMaster>()) return
        guiy { GuildMainMenu(player, feature) }

//        if((clickedCooldown[player.uniqueId] ?: 0) < Bukkit.getCurrentTick()) {
//            clickedCooldown[player.uniqueId] = Bukkit.getCurrentTick() + 5
//            guiy { GuildMainMenu(player) }
//        }
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

    @EventHandler(priority = EventPriority.LOWEST)
    fun AsyncPlayerChatEvent.overrideVentureChat() {
        if (player.playerData.guildChatStatus && !player.hasGuild()) {
            player.error("You cannot use guild chat without a guild.")
            player.success("Guild chat has been toggled OFF")
            return
        }

        if (player.playerData.guildChatStatus && player.hasGuild()) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun AsyncPlayerChatEvent.toggleGuildChat(feature: GuildFeature) {

        isCancelled = false
        if (!player.playerData.guildChatStatus || !player.hasGuild()) return

        recipients.clear()
        recipients.add(player)
        format = "${feature.guildChatPrefix}${player.displayName}: $message"

        Bukkit.getOnlinePlayers().forEach {
            if (it.getGuildName().isEmpty()) return@forEach
            if (it.getGuildName().lowercase() == player.getGuildName().lowercase()) recipients.add(it)
        }
    }
}
