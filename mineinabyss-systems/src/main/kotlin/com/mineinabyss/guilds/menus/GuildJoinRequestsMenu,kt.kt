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
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.mineinabyss.data.GuildJoinQueue
import com.mineinabyss.mineinabyss.data.GuildJoinType
import com.mineinabyss.mineinabyss.data.Players
import com.mineinabyss.mineinabyss.extensions.addMemberToGuild
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

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
        }.single()[Players.guildId]

        val players = GuildJoinQueue.select {
            (GuildJoinQueue.guildId eq id) and (GuildJoinQueue.joinType eq GuildJoinType.Request)
        }.single()[GuildJoinQueue.playerUUID].broadcastVal()

        Players.select {
            Players.playerUUID eq players
        }.map { row -> row[Players.playerUUID.broadcastVal()] }
    }
    Grid(9, 4, modifier) {
        broadcast("test")
        requests.forEach { newMember ->
            broadcast(newMember)
            val guildItem = ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.YELLOW}${ChatColor.ITALIC}${newMember.toPlayer()?.name}")
                lore = listOf("${ChatColor.BLUE}Click this to accept or deny the join-request.",
                )
            }
            Item(guildItem, Modifier.clickable {
                player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                guiy { HandleJoinRequests(player, newMember) }
            })
        }
    }
}

@Composable
fun GuiyOwner.HandleJoinRequests(player: Player, newMember: UUID) {
    Chest(listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:handle_guild_join_requests:",
        5, onClose = { exit() }) {
        PlayerLabel(player, Modifier.at(4,0), newMember)
        AcceptGuildRequest(player, Modifier.at(1,2), newMember)
        DeclineGuildRequest(player, Modifier.at(5,2))
        PreviousMenuButton(player, Modifier.at(4,4))
    }
}

@Composable
fun PlayerLabel(player: Player, modifier: Modifier, newMember: UUID) {
    Grid(2,2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
    }){
        Item(ItemStack(Material.PAPER).editItemMeta {
            setDisplayName("${ChatColor.YELLOW}${ChatColor.ITALIC}${newMember.toPlayer()?.name}")
        })
    }
}

@Composable
fun AcceptGuildRequest(player: Player, modifier: Modifier, newMember: UUID) {


    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.addMemberToGuild(newMember.toPlayer()!!)
    }){
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GREEN}Accept Invite")
                //setCustomModelData(1)
            })
        }
    }
}

@Composable
fun DeclineGuildRequest(player: Player, modifier: Modifier) {
    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        /* Remove invite from table */
    }){
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}Decline Invite")
                //setCustomModelData(1)
            })
        }
    }
}
