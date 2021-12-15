package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.data.GuildJoinQueue
import com.mineinabyss.mineinabyss.data.GuildJoinType
import com.mineinabyss.mineinabyss.data.Players
import com.mineinabyss.mineinabyss.extensions.*
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuiyOwner.GuildJoinRequestsMenu(player: Player) {
    Chest(listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:handle_guild_join_requests:",
        5, onClose = { exit() }) {
        GuildJoinRequests(player, Modifier.at(1,1))
        PreviousMenuButton(player, Modifier.at(2, 4))
    }
}

@Composable
fun GuildJoinRequests(player: Player, modifier: Modifier) {
    /* Transaction to query GuildInvites and playerUUID */
    val requests = transaction {
        val id = Players.select {
            Players.playerUUID eq player.uniqueId
        }.first()[Players.guildId]

        GuildJoinQueue.select {
            (GuildJoinQueue.guildId eq id) and (GuildJoinQueue.joinType eq GuildJoinType.Request)
        }.map { row -> row[GuildJoinQueue.playerUUID] }
    }
    Grid(9, 4, modifier) {
        requests.forEach { newMember ->
            val guildItem = ItemStack(Material.PLAYER_HEAD).editItemMeta {
                if (this is SkullMeta) {
                    setDisplayName("${ChatColor.YELLOW}${ChatColor.ITALIC}${newMember.toPlayer()?.name}")
                    lore = listOf(
                        "${ChatColor.BLUE}Click this to accept or deny the join-request.",
                    )
                    owningPlayer = newMember.toPlayer()
                }
            }
            Item(guildItem, Modifier.clickable {
                player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                guiy { HandleJoinRequests(player, newMember.toPlayer()!!) }
            })
        }
    }
}

@Composable
fun GuiyOwner.HandleJoinRequests(player: Player, newMember: OfflinePlayer) {
    Chest(listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:handle_guild_join_requests:",
        5, onClose = { exit() }) {
        PlayerLabel(player, Modifier.at(4,0), newMember)
        AcceptGuildRequest(player, Modifier.at(1,2), newMember)
        DeclineGuildRequest(player, Modifier.at(5,2), newMember)
        DeclineAllGuildRequests(player, Modifier.at(8,4))
        PreviousMenuButton(player, Modifier.at(4,4))
    }
}

@Composable
fun PlayerLabel(player: Player, modifier: Modifier, newMember: OfflinePlayer) {
    Grid(2,2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
    }){
        Item(ItemStack(Material.PAPER).editItemMeta {
            setDisplayName("${ChatColor.YELLOW}${ChatColor.ITALIC}${newMember.name}")
        })
    }
}

@Composable
fun AcceptGuildRequest(player: Player, modifier: Modifier, newMember: OfflinePlayer) {
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        if (player.getGuildJoinType() == GuildJoinType.Request){
            player.error("Your guild is in 'Invite-only' mode.")
            player.error("Change it to 'Any' or 'Request-only' mode to accept requests.")
            return@clickable
        }
        player.addMemberToGuild(newMember)
        if (player.getGuildMemberCount() < player.getGuildLevel().times(5).plus(1)){
            newMember.removeGuildQueueEntries(GuildJoinType.Request)
        }
        guiy { GuildMemberManagementMenu(player) }
    }){
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GREEN}Accept Join-Request")
                //setCustomModelData(1)
            })
        }
    }
}

@Composable
fun DeclineGuildRequest(player: Player, modifier: Modifier, newMember: OfflinePlayer) {
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        newMember.removeGuildQueueEntries(GuildJoinType.Request)
        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}❌ ${ChatColor.YELLOW}You denied the join-request from ${newMember.name}")
        guiy { GuildMemberManagementMenu(player) }
    }){
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}Decline Join-Request")
                //setCustomModelData(1)
            })
        }
    }
}

@Composable
fun DeclineAllGuildRequests(player: Player, modifier: Modifier) {
    Item(ItemStack(Material.PAPER).editItemMeta {
        setDisplayName("${ChatColor.RED}Decline All Join-Request")
        //setCustomModelData(1)
    }, modifier.clickable {
        player.removeGuildQueueEntries(GuildJoinType.Request, true)
        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}❌ ${ChatColor.YELLOW}You denied all join-requests for your guild!")
        guiy { GuildMemberManagementMenu(player) }
    })
}
