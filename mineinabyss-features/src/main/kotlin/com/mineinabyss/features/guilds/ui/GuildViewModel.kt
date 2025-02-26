package com.mineinabyss.features.guilds.ui

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.data.GuildJoinRequestsRepository
import com.mineinabyss.features.guilds.data.GuildMessagesRepository
import com.mineinabyss.features.guilds.data.GuildRepository
import com.mineinabyss.features.guilds.data.entities.GuildEntity
import com.mineinabyss.features.guilds.data.entities.GuildJoinEntity
import com.mineinabyss.features.guilds.data.tables.GuildJoinRequestsTable
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.data.tables.GuildMessagesTable
import com.mineinabyss.features.guilds.data.tables.GuildRank
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
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.insert
import java.util.*

data class GuildUiState(
    val id: Int,
    val name: String,
    val owner: GuildMemberUiState,
    val level: Int,
    val memberCount: Int,
    val members: List<GuildMemberUiState>,
    val balance: Int,
    val joinType: GuildJoinType,
)

data class GuildMemberUiState(
    val name: String,
    val uuid: UUID,
    val rank: GuildRank,
    val currentGuild: Int,
)

data class Invite(val guild: GuildUiState)

class GuildViewModel(
    val player: Player,
    val openedFromHQ: Boolean,
    private val guildRepo: GuildRepository,
    private val messagesRepo: GuildMessagesRepository,
    private val requestsRepo: GuildJoinRequestsRepository,
) : GuiyViewModel() {
    private val _currentGuild = MutableStateFlow<GuildUiState?>(null)
    private val _memberInfo = MutableStateFlow<GuildMemberUiState?>(null)
    val currentGuild = _currentGuild.asStateFlow()
    val memberInfo = _memberInfo.asStateFlow()
    val invites = MutableStateFlow<List<Invite>>(emptyList())
    val joinRequests = MutableStateFlow<List<Invite>>(emptyList())
    val nav = GuildNav { GuildScreen.Default(player) }

    val isCaptainOrAbove = _memberInfo.map {
        val rank = it?.rank
        rank == GuildRank.CAPTAIN || rank == GuildRank.OWNER
    }.stateIn(viewModelScope, WhileSubscribed, initialValue = false)

    val canAcceptNewMembers = _currentGuild.map {
        if (it == null) false
        else it.memberCount < it.level * 5
    }.stateIn(viewModelScope, WhileSubscribed, initialValue = false)

    init {
        viewModelScope.launch {
            _memberInfo.emit(guildRepo.member(player.uniqueId))
            _currentGuild.emit(guildRepo.guildForPlayer(player.uniqueId))
            invites.emit(guildRepo.getInvites(player.uniqueId))
        }
    }

    fun invitePlayer(playerName: String) = viewModelScope.launch {
        val guild = currentGuild.value ?: return@launch
        val invitedMember = Bukkit.getOfflinePlayerIfCached(playerName)
        if (invitedMember == null) {
            player.error("Player $playerName could not be found.")
            return@launch
        }
        val inviteMessage ="<yellow>You have been invited to join the <gold>${guild.name}</gold> guild."

        /* Should invites be cancelled if player already is in one? */
        /* Or should this be checked when a player tries to accept an invite? */
        if(guildRepo.member(invitedMember.uniqueId) != null) {
            player.error("This player is already in another guild!")
            return@launch
        }

        val existingInvite = requestsRepo.getRequest(invitedMember.uniqueId, guild.id)

        if (existingInvite == GuildJoinType.INVITE) {
            player.error("This player has already been invited to your guild!")
            return@launch
        }

        if (existingInvite == GuildJoinType.REQUEST) {
            player.error("This player has already requested to join your guild!")
            player.error("Navigate to the <b>Manage GuildJoin REQUEST</b> menu to respond.")
            return@launch
        }

        requestsRepo.addRequest(invitedMember.uniqueId, guild.id, GuildJoinType.INVITE)

        player.success("${invitedMember.name} was invited to your guild!")
        messagesRepo.messagePlayer(invitedMember.uniqueId, inviteMessage)
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

    fun updateJoinType() = viewModelScope.launch {
        val guild = currentGuild.value ?: return@launch
        guildRepo.updateJoinType(guild.id, guild.joinType.next())
    }

    fun deleteGuild() = viewModelScope.launch {
        // TODO migrate all checks over
//        val guild = _currentGuild.value ?: return@launch
//        guildRepo.deleteGuild(guild.id)
    }
}
