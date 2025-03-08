package com.mineinabyss.features.guilds.ui

import com.mineinabyss.features.guilds.extensions.GuildName
import com.mineinabyss.features.guilds.extensions.getGuildLevel
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import org.bukkit.OfflinePlayer

sealed class GuildScreen(var title: String, val height: Int) {
    object Default
    object GuildInfo

    data object Leave : GuildScreen(":space_-8::guild_disband_or_leave_menu:", 5)
    data object Disband : GuildScreen(":space_-8::guild_disband_or_leave_menu:", 5)

    data object GuildList : GuildScreen(":space_-8::guild_list_menu:", 6)
    class GuildLookupMembers(val guildName: GuildName) :
        GuildScreen(":space_-8:${":guild_lookup_members${minOf(guildName.getGuildLevel(), 3)}"}:", minOf(guildName.getGuildLevel() + 3,
            MAX_CHEST_HEIGHT
        ))

    // Forgot to add to pack so this is fine for now
    data object InviteList : GuildScreen(":space_-8::guild_inbox_list_menu:", 5)
    class InviteScreen(val guildId: GuildUiState) : GuildScreen(":space_-8::guild_inbox_handle_menu:", 5)

    data object JoinRequestList : GuildScreen(":space_-8::guild_inbox_list_menu:", 5)
    class JoinRequest(val from: OfflinePlayer) : GuildScreen(":space_-8::guild_inbox_handle_menu:", 5)

    class MemberOptions(val member: OfflinePlayer) :
        GuildScreen(":space_-8::guild_member_action_menu:", 5)

    object MemberList//(val guildLevel: Int, player: Player) :
//        GuildScreen(":space_-8:${DecideMenus.decideMemberMenu(player, player.getGuildJoinType())}", minOf(guildLevel + 2,
//            MAX_CHEST_HEIGHT
//        ))
    object RenameGuild
    object CreateGuild
}
