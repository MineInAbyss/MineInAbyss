package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.data.Players
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuiyOwner.GuildMemberManagementMenu(player: Player) {
    Chest(
        listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:guild_member_management_menu:",
        6, onClose = { exit() }) {
        ManageGuildMembersButton(player, Modifier.at(1, 1))
        InviteToGuildButton(player, Modifier.at(3, 1))
        ManageGuildJoinRequestButton(player, Modifier.at(5,1))
    }
}

@Composable
fun ManageGuildMembersButton(player: Player, modifier: Modifier) {
    transaction {
        val playerRow = Players.select {
            Players.playerUUID eq player.uniqueId
        }.single()

        val guildId = playerRow[Players.guildId]
        val memberRank = playerRow[Players.guildRank]

        /* Message to all guild-members */
        Players.select {
            (Players.guildId eq guildId) and
            (Players.playerUUID neq player.uniqueId) and
            ((Players.guildRank neq memberRank) or (Players.guildRank.less(memberRank)))
        }.forEach { row ->
            val member = Bukkit.getOfflinePlayer(row[Players.playerUUID])

            // TODO Fix this and make it sorted by rank etc
            //GuildMembers(player, modifier.at(1,1), member as Player)

            guiy {
                Grid(1, 1, modifier.clickable {
                    player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                    guiy { GuildMemberAction(player, member) }
                })
                {
                    /* Display player head in 2D */
                    repeat(1) {
                        Item(ItemStack(Material.PAPER).editItemMeta {
                            setDisplayName("${ChatColor.YELLOW}${ChatColor.ITALIC}${member}")
                        })
                    }
                }
            }
        }
    }
}

//@Composable
//fun GuildMembers(player: Player, modifier: Modifier, member: Player) {
//    Grid(1, 1, modifier.clickable {
//        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
//        guiy { GuildMemberAction(player, member) }
//    })
//    {
//        /* Display player head in 2D */
//        repeat(1) {
//            Item(ItemStack(Material.PAPER).editItemMeta {
//                setDisplayName("${ChatColor.YELLOW}${ChatColor.ITALIC}${player.name()}")
//            })
//        }
//    }
//}

@Composable
fun InviteToGuildButton(player: Player, modifier: Modifier) {
    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
    })
    {
        repeat(1) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}${ChatColor.ITALIC}Invite To Guild")
            })
        }
    }
}

@Composable
fun ManageGuildJoinRequestButton(player: Player, modifier: Modifier) {
    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
    })
    {
        repeat(1) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}${ChatColor.ITALIC}Guild Join Requests")
            })
        }
    }
}
