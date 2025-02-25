package com.mineinabyss.features.guilds.menus

import com.mineinabyss.features.guilds.extensions.GuildName
import com.mineinabyss.features.guilds.extensions.getGuildJoinType
import com.mineinabyss.features.guilds.extensions.getGuildLevel
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

sealed class GuildScreen(var title: String, val height: Int) {
    class Default(player: Player) : GuildScreen(title = ":space_-8:${DecideMenus.decideMainMenu(player)}", height = 4)

    class GuildInfo(isGuildOwner: Boolean) : GuildScreen(":space_-8:${DecideMenus.decideInfoMenu(isGuildOwner)}", 6)

    data object Leave : GuildScreen(":space_-8::guild_disband_or_leave_menu:", 5)
    data object Disband : GuildScreen(":space_-8::guild_disband_or_leave_menu:", 5)

    data object GuildList : GuildScreen(":space_-8::guild_list_menu:", 6)
    class GuildLookupMembers(val guildName: GuildName) :
        GuildScreen(":space_-8:${":guild_lookup_members${minOf(guildName.getGuildLevel(), 3)}"}:", minOf(guildName.getGuildLevel() + 3,
            MAX_CHEST_HEIGHT
        ))

    // Forgot to add to pack so this is fine for now
    data object InviteList : GuildScreen(":space_-8::guild_inbox_list_menu:", 5)
    class Invite(val owner: OfflinePlayer) : GuildScreen(":space_-8::guild_inbox_handle_menu:", 5)

    data object JoinRequestList : GuildScreen(":space_-8::guild_inbox_list_menu:", 5)
    class JoinRequest(val from: OfflinePlayer) : GuildScreen(":space_-8::guild_inbox_handle_menu:", 5)

    class MemberOptions(val member: OfflinePlayer) :
        GuildScreen(":space_-8::guild_member_action_menu:", 5)

    class MemberList(val guildLevel: Int, player: Player) :
        GuildScreen(":space_-8:${DecideMenus.decideMemberMenu(player, player.getGuildJoinType())}", minOf(guildLevel + 2,
            MAX_CHEST_HEIGHT
        ))
}
