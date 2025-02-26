package com.mineinabyss.features.guilds.ui

import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.extensions.getGuildLevel
import com.mineinabyss.features.guilds.extensions.hasGuild
import com.mineinabyss.features.guilds.extensions.hasGuildInvites
import com.mineinabyss.features.guilds.extensions.hasGuildRequests
import org.bukkit.entity.Player

object DecideMenus {
    fun decideMainMenu(player: Player): String {
        return buildString {
            append(":guild_main_menu:")
            append(":space_-138:")
            if (player.hasGuild()) append(":guild_main_menu_info:")
            else append(":guild_main_menu_create:")
            append(":space_66:")
            if (player.hasGuildInvites()) append(":guild_inbox_unread:")
            else append(":guild_inbox_read:")
        }
    }

    fun decideInfoMenu(isGuildOwner: Boolean): String {
        return buildString {
            append(":guild_info_menu:")
            append(":space_-28:")
            if (isGuildOwner) {
                append(":guild_disband_button:")
                append(":space_-18:")
                append(":guild_level_up_button:")
            } else append(":guild_leave_button:")
        }
    }

    //TODO Implement lists for guilds, making one able to have more than 5(25) members
    fun decideMemberMenu(player: Player, joinType: GuildJoinType): String {
        val menuHeight = minOf(player.getGuildLevel(), 4)
        return buildString {
            append(":guild_member_management_menu_${menuHeight}:")
            append(":space_-171:")
            append(":guild_member_management_jointype_${joinType.name.lowercase()}:")
            append(":space_125:")
            if (player.hasGuildRequests()) append(":guild_inbox_unread:")
            else append(":guild_inbox_read:")
        }
    }
}
