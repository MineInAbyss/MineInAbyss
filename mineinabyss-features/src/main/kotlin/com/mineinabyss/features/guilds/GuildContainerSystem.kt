package com.mineinabyss.features.guilds

import com.mineinabyss.features.guilds.database.GuildRank
import com.mineinabyss.features.guilds.extensions.getGuildName
import com.mineinabyss.features.guilds.extensions.getGuildRank
import com.mineinabyss.features.guilds.extensions.hasGuild
import nl.rutgerkok.blocklocker.group.GroupSystem
import org.bukkit.entity.Player

class GuildContainerSystem : GroupSystem() {

    override fun isInGroup(player: Player, guildName: String): Boolean {
        val name = player.getGuildName()?.replace(" ", "_")
        return name.equals(guildName, true) && player.hasGuild()
    }

    override fun isGroupLeader(player: Player, groupName: String): Boolean {
        val guild = player.hasGuild()
        if (!guild) return false

        return player.getGuildRank() == GuildRank.OWNER
    }
}
