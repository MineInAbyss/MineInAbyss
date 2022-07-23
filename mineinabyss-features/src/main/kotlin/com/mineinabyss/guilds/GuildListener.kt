package com.mineinabyss.guilds

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.mineinabyss.chatty.helpers.emoteFixer
import com.mineinabyss.chatty.helpers.serializeLegacy
import com.mineinabyss.chatty.helpers.stripTags
import com.mineinabyss.chatty.listeners.RendererExtension
import com.mineinabyss.components.guilds.GuildMaster
import com.mineinabyss.components.guilds.SpyOnGuildChat
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.guilds.database.GuildRanks
import com.mineinabyss.guilds.extensions.getGuildName
import com.mineinabyss.guilds.extensions.getGuildRank
import com.mineinabyss.guilds.extensions.hasGuild
import com.mineinabyss.guilds.menus.GuildMainMenu
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.helpers.MessageQueue
import com.mineinabyss.helpers.MessageQueue.content
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.messaging.serialize
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.core.mineInAbyss
import github.scarsz.discordsrv.api.ListenerPriority
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import nl.rutgerkok.blocklocker.group.GroupSystem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.seconds

class GuildListener(private val feature: GuildFeature) : Listener {

    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteractGuildMaster() {
        rightClicked.toGearyOrNull()?.get<GuildMaster>() ?: return
        guiy { GuildMainMenu(player, feature, true) }
    }

    @EventHandler
    suspend fun PlayerJoinEvent.onJoin() {
        delay(1.seconds)
        withContext(mineInAbyss.asyncDispatcher) {
            transaction(AbyssContext.db) {
                MessageQueue.select { MessageQueue.playerUUID eq player.uniqueId }.forEach {
                    player.info(it[content])
                }
                MessageQueue.deleteWhere { MessageQueue.playerUUID eq player.uniqueId }
            }
        }
    }
}

class GuildContainerSystem : GroupSystem() {

    override fun isInGroup(player: Player, guildName: String): Boolean {
        val name = player.getGuildName().replace(" ", "_")
        return name.equals(guildName, true) && player.hasGuild()
    }

    override fun isGroupLeader(player: Player, groupName: String): Boolean {
        val guild = player.hasGuild()
        if (!guild) return false

        return player.getGuildRank() == GuildRanks.Owner
    }

}

class GuildChatSystem(private val feature: GuildFeature) : Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun AsyncChatEvent.onGuildChat() {
        if (!player.playerData.guildChatStatus || !player.hasGuild()) return
        viewers().clear()
        message("${feature.guildChatPrefix}${player.displayName().serialize()}: ${originalMessage().serialize()}".miniMsg())
        Bukkit.getOnlinePlayers().forEach {
            when {
                it.toGeary().has<SpyOnGuildChat>() -> viewers().add(it)
                it.getGuildName() == player.getGuildName() -> viewers().add(it)
                else -> return@forEach
            }
        }
        viewers().forEach {
            RendererExtension().render(player, player.displayName(), message(), it)
        }
        isCancelled = true
        viewers().clear()
    }

    @Subscribe(priority = ListenerPriority.LOW)
    fun GameChatMessagePreProcessEvent.onChat() {
        if (player.playerData.guildChatStatus) {
            isCancelled = true
            return
        }
    }

    private fun Component.cleanUpHackyFix() =
        this.replaceText(TextReplacementConfig.builder().match("<<").replacement("<").build())


    private fun String.cleanUpHackyFix() =
        this.replace("<<", "<").serializeLegacy().stripTags()

    private fun String.translateEmoteIDs(): String {
        var translated = this
        emoteFixer.emotes.entries.forEach { (emoteId, replacement) ->
            val id = ":$emoteId:"
            if (id in this) {
                translated = translated.replace(id, replacement)
            }
        }
        return translated.cleanUpHackyFix()
    }

    private fun Component.translateEmoteIDsToComponent(): Component {
        var translated = this
        emoteFixer.emotes.entries.forEach { (emoteId, replacement) ->
            val id = ":$emoteId:"
            if (id in translated.deserialize()) {
                translated = translated.replaceText(
                    TextReplacementConfig.builder().match(id)
                        .replacement("<$replacement".miniMessage()).build()
                )
            }
        }
        return translated.cleanUpHackyFix()
    }

    private fun Component.deserialize(): String {
        return MiniMessage.builder().build()
            .serialize(this)
    }

    private fun String.miniMessage(): Component {
        return MiniMessage.builder().build()
            .deserialize(this)
    }
}
