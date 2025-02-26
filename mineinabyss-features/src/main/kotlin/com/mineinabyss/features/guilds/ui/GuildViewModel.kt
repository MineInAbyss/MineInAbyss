package com.mineinabyss.features.guilds.ui

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.data.GuildMessagesRepository
import com.mineinabyss.features.guilds.data.GuildRepository
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.data.tables.GuildRank
import com.mineinabyss.features.guilds.data.entities.GuildEntity
import com.mineinabyss.features.guilds.extensions.guildChat
import com.mineinabyss.features.guilds.extensions.guildChatId
import com.mineinabyss.features.helpers.ui.WhileSubscribed
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.guiy.inventory.GuiyViewModel
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import java.util.*

data class GuildUiState(
    val id: Int,
    val name: String,
    val owner: GuildMemberUiState,
    val level: Int,
    val memberCount: Int,
    val balance: Int,
)

data class GuildMemberUiState(
    val name: String,
    val uuid: UUID,
    val rank: GuildRank,
)

data class Invite(val guild: GuildUiState)

class GuildViewModel(
    val player: Player,
    val openedFromHQ: Boolean,
    private val guildRepo: GuildRepository,
    private val messagesRepo: GuildMessagesRepository,
) : GuiyViewModel() {
    private val _currentGuild = MutableStateFlow<GuildUiState?>(null)
    private val _memberInfo = MutableStateFlow<GuildMemberUiState?>(null)
    val currentGuild = _currentGuild.asStateFlow()
    val memberInfo = _memberInfo.asStateFlow()
    val invites = MutableStateFlow<List<Invite>>(emptyList())
    val nav = GuildNav { GuildScreen.Default(player) }

    val isCaptainOrAbove = _memberInfo.map {
        val rank = it?.rank
        rank == GuildRank.CAPTAIN || rank == GuildRank.OWNER
    }.stateIn(viewModelScope, WhileSubscribed, initialValue = false)

    val canAcceptNewMembers = _currentGuild.map {
        if(it == null) false
        else it.memberCount < it.level * 5
    }.stateIn(viewModelScope, WhileSubscribed, initialValue = false)

    init {
        viewModelScope.launch {
            _memberInfo.emit(guildRepo.member(player.uniqueId))
            _currentGuild.emit(guildRepo.guildForPlayer(player.uniqueId))
            invites.emit(guildRepo.getInvites(player.uniqueId))
        }
    }

    fun acceptInvite(guildId: Int) = viewModelScope.launch {
        val guild = guildRepo.getGuild(guildId)
        if (guild == null) {
            nav.reset()
            player.error("This guild does not exist anymore!")
            return@launch
        }

        if (guild.joinType == GuildJoinType.REQUEST) {
            player.error("This guild is in 'REQUEST-only' mode.")
            player.error("Change it to 'ANY' or 'INVITE-only' mode to accept invites.")
            return@launch
        }
        if (guild.members.count() >= guild.level * 5) {
            player.error("This guild has reached its current member cap!")
            return@launch
        }


        if (guildRepo.addMember(guildId, player.uniqueId))
            return@launch player.error("Failed to join ${guild.name}.")

        nav.back()
    }

    fun declineInvite(guildId: Int) = viewModelScope.launch {
        val guild = guildRepo.getGuild(guildId)
        if (guild == null) {
            player.error("This guild does not exist anymore!")
            return@launch
        }
        guildRepo.clearInvite(guildId, player.uniqueId)
        player.info("<gold><b>❌</b> <yellow>You denied the invite from </yellow><i>${guild.name}")
        if (invites.value.size > 1) nav.back()
        else nav.reset()
    }

    //TODO Make sure guild chatname is properly updated when guild name is changed
    fun rename(newName: String) = viewModelScope.launch {
        val guild = _currentGuild.value ?: run {
            player.error("You are not in a guild")
            return@launch
        }
        val oldName = guild.name
        if (!guildRepo.findGuildByName(newName).empty()) {
            player.error("There is already a guild registered with this name!")
            return@launch
        }

        //TODO move more stuff to repo
        GuildEntity.findByIdAndUpdate(guild.id) {
            it.name = newName
        }

        // Update the guildchat ID on online players, rest handled on join
        withContext(abyss.plugin.minecraftDispatcher) {
            guildRepo.getMembers(guild.id).mapNotNull { it.uuid.value.toPlayer() }.forEach {
                val gearyPlayer = it.toGeary()
                val channelData = gearyPlayer.get<ChannelData>() ?: return@forEach
                if (channelData.channelId == oldName.guildChatId())
                    gearyPlayer.setPersisting(channelData.copy(channelId = oldName.guildChatId()))
                chatty.config.channels -= oldName.guildChatId()
                newName.guildChat()
            }
        }

        messagesRepo.messageAllGuildMembers(
            guild.id,
            "<yellow>The Guild you are in has been renamed to <gold><i>$newName!",
            exclude = setOf(player.uniqueId)
        )
        player.success("Your guild was successfully renamed to <gold><i>$newName!")
    }
}
