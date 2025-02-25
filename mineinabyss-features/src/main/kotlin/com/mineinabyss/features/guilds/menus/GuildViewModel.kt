package com.mineinabyss.features.guilds.menus

import com.mineinabyss.features.guilds.extensions.getGuildBalance
import com.mineinabyss.features.guilds.extensions.getGuildLevel
import com.mineinabyss.features.guilds.extensions.getGuildMemberCount
import com.mineinabyss.features.guilds.extensions.getGuildName
import com.mineinabyss.features.guilds.extensions.getGuildOwner
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.GuiyViewModel
import org.bukkit.entity.Player

class GuildViewModel(
    val player: Player,
    val owner: GuiyOwner,
    val openedFromHQ: Boolean,
): GuiyViewModel() {
    //TODO cache more than just guild level here
    val guildName get() = player.getGuildName()
    val guildLevel get() = player.getGuildLevel()
    val guildOwner get() = player.getGuildOwner()?.toOfflinePlayer()
    val memberCount get() = player.getGuildMemberCount()
    val guildBalance get() = player.getGuildBalance()
    val nav = GuildNav { GuildScreen.Default(player) }
}
