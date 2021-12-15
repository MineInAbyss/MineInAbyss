package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.data.GuildRanks
import com.mineinabyss.mineinabyss.data.Players
import com.mineinabyss.mineinabyss.extensions.getGuildLevel
import com.mineinabyss.mineinabyss.extensions.getGuildRank
import com.mineinabyss.mineinabyss.extensions.leaveGuild
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction


@Composable
fun GuiyOwner.CurrentGuildMenu(player: Player) {
    Chest(
        listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:current_guild_menu:",
        player.getGuildLevel() + 2, onClose = { exit() }) {
        GuildMemberList(player, Modifier.at(1, 1))
        if (player.getGuildRank() != GuildRanks.Owner){
            LeaveGuildButton(player, Modifier.at(8,player.getGuildLevel() + 1))
        }
        PreviousMenuButton(player, Modifier.at(2, player.getGuildLevel() + 1))
    }
}


@Composable
fun GuildMemberList(player: Player, modifier: Modifier) {
    val members = transaction {
        val players = Players.select {
            Players.playerUUID eq player.uniqueId
        }.single()

        val guildId = players[Players.guildId]

        Players.select {
            Players.guildId eq guildId
        }.map { row ->
            Pair(row[Players.guildRank], Bukkit.getOfflinePlayer(row[Players.playerUUID]))
        }
    }
    Grid(5, player.getGuildLevel(), modifier){
        members.sortedBy { it.first }.forEach { (rank, member) ->
            Item(ItemStack(Material.PLAYER_HEAD).editItemMeta {
                if (this is SkullMeta) {
                    setDisplayName("${ChatColor.GOLD}${ChatColor.ITALIC}${member.name}")
                    lore = listOf(
                        "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Rank: ${ChatColor.YELLOW}${ChatColor.ITALIC}${member.getGuildRank()}",
                    )
                    owningPlayer = member
                }
            })
        }
    }
}

@Composable
fun LeaveGuildButton(player: Player, modifier: Modifier) {
    Grid(2, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.leaveGuild()
        player.closeInventory()
    })
    {
        repeat(4) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}${ChatColor.ITALIC}Leave Guild")
            })
        }
    }
}


