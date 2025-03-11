package com.mineinabyss.features.guilds.ui

import com.mineinabyss.features.guilds.extensions.GuildName
import com.mineinabyss.features.guilds.extensions.getGuildLevel
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import org.bukkit.OfflinePlayer
import java.util.UUID

sealed class GuildScreen(var title: String, val height: Int) {
    object Default
    object GuildInfo

    data object Leave : GuildScreen(":space_-8::guild_disband_or_leave_menu:", 5)
    data object Disband : GuildScreen(":space_-8::guild_disband_or_leave_menu:", 5)

    data object GuildList : GuildScreen(":space_-8::guild_list_menu:", 6)
    class GuildLookupMembers(val guild: GuildUiState)

    // Forgot to add to pack so this is fine for now
    data object InviteList : GuildScreen(":space_-8::guild_inbox_list_menu:", 5)
    class InviteScreen(val invite: Invite)

    data object JoinRequestList : GuildScreen(":space_-8::guild_inbox_list_menu:", 5)
//    class JoinRequest(val from: OfflinePlayer) : GuildScreen(":space_-8::guild_inbox_handle_menu:", 5)

    class MemberOptions(val member: UUID)

    object MemberList//(val guildLevel: Int, player: Player) :
//        GuildScreen(":space_-8:${DecideMenus.decideMemberMenu(player, player.getGuildJoinType())}", minOf(guildLevel + 2,
//            MAX_CHEST_HEIGHT
//        ))
    object RenameGuild
    object CreateGuild
}
