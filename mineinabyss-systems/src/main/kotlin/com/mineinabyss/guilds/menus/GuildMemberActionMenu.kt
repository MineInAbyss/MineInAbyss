package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.mineinabyss.extensions.kickPlayerFromGuild
import com.mineinabyss.mineinabyss.extensions.promotePlayerInGuild
import org.bukkit.ChatColor.*
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.GuildMemberAction(player: Player, member: OfflinePlayer) {
    Chest(
        listOf(player), "${Space.of(-18)}$WHITE:guild_member_action_menu:",
        5, onClose = { exit() }) {
        /* Large playerhead or playermodel :pogo: */
        PromoteGuildMember(player, Modifier.at(1, 1), member)
        KickGuildMember(player, Modifier.at(5, 1), member)
        PreviousMenuButton(player, Modifier.at(2, 4))
    }
}

@Composable
fun PromoteGuildMember(player: Player, modifier: Modifier, member: OfflinePlayer) {
    Item(
        TitleItem.of("$BLUE${ITALIC}Promote Member"),
        modifier.size(3, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            player.promotePlayerInGuild(member)
            guiy { GuildMemberManagementMenu(player) }
        }
    )
}

@Composable
fun KickGuildMember(player: Player, modifier: Modifier, member: OfflinePlayer) {
    Item(
        TitleItem.of("$RED${ITALIC}Kick Member"),
        modifier.size(3, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            player.kickPlayerFromGuild(member)
            guiy { GuildMemberManagementMenu(player) }
        }
    )
}


