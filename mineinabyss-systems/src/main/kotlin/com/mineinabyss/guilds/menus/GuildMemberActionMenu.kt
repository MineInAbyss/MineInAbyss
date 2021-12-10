package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.extensions.kickPlayerFromGuild
import com.mineinabyss.mineinabyss.extensions.promotePlayerInGuild
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.GuildMemberAction(player: Player, member: OfflinePlayer) {
    Chest(
        listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:guild_member_action_menu:",
        6, onClose = { exit() }) {
        /* Large playerhead or playermodel :pogo: */
        PromoteGuildMember(player, Modifier.at(2, 1), member)
        KickGuildMember(player, Modifier.at(4,1), member)
    }
}

@Composable
fun PromoteGuildMember(player: Player, modifier: Modifier, member: OfflinePlayer) {
    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.promotePlayerInGuild(member)
    })
    {
        repeat(1) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}${ChatColor.ITALIC}Promote Member")
            })
        }
    }
}

@Composable
fun KickGuildMember(player: Player, modifier: Modifier, member: OfflinePlayer) {
    val data = player.playerData
    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.kickPlayerFromGuild(member)
    })
    {
        repeat(1) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}${ChatColor.ITALIC}Kick Member")
            })
        }
    }
}
