package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.data.GuildJoinQueue
import com.mineinabyss.mineinabyss.data.GuildJoinType
import com.mineinabyss.mineinabyss.data.Players
import com.mineinabyss.mineinabyss.extensions.*
import org.bukkit.ChatColor.*
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
    Chest(listOf(player), "${Space.of(-18)}$WHITE:handle_guild_join_requests:",
        5, onClose = { exit() }) {
        GuildJoinRequests(player, Modifier.at(1, 1))
        PreviousMenuButton(player, Modifier.at(2, 4))
    }
}

@Composable
fun GuildJoinRequests(player: Player, modifier: Modifier) {
    /* Transaction to query GuildInvites and playerUUID */
    val requests = transaction(AbyssContext.db) {
        val id = Players.select {
            Players.playerUUID eq player.uniqueId
        }.first()[Players.guildId]

        GuildJoinQueue.select {
            (GuildJoinQueue.guildId eq id) and (GuildJoinQueue.joinType eq GuildJoinType.Request)
        }.map { row -> row[GuildJoinQueue.playerUUID] }
    }
    Grid(modifier.size(9, 4)) {
        requests.forEach { newMember ->
            val guildItem = ItemStack(Material.PLAYER_HEAD).editItemMeta {
                if (this is SkullMeta) {
                    setDisplayName("$YELLOW$ITALIC${newMember.toPlayer()?.name}")
                    lore = listOf(
                        "${BLUE}Click this to accept or deny the join-request.",
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
    Chest(listOf(player), "${Space.of(-18)}$WHITE:handle_guild_join_requests:",
        5, onClose = { exit() }) {
        PlayerLabel(player, Modifier.at(4, 0), newMember)
        AcceptGuildRequest(player, Modifier.at(1, 2), newMember)
        DeclineGuildRequest(player, Modifier.at(5, 2), newMember)
        DeclineAllGuildRequests(player, Modifier.at(8, 4))
        PreviousMenuButton(player, Modifier.at(4, 4))
    }
}

@Composable
fun PlayerLabel(player: Player, modifier: Modifier, newMember: OfflinePlayer) {
    Item(
        TitleItem.of("$YELLOW$ITALIC${newMember.name}"),
        modifier.size(2, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        }
    )
}

@Composable
fun AcceptGuildRequest(player: Player, modifier: Modifier, newMember: OfflinePlayer) {
    Item(
        TitleItem.of("${GREEN}Accept Join-Request"),
        modifier.size(3, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            if (player.getGuildJoinType() == GuildJoinType.Request) {
                player.error("Your guild is in 'Invite-only' mode.")
                player.error("Change it to 'Any' or 'Request-only' mode to accept requests.")
                return@clickable
            }
            player.addMemberToGuild(newMember)
            if (player.getGuildMemberCount() < player.getGuildLevel().times(5).plus(1)) {
                newMember.removeGuildQueueEntries(GuildJoinType.Request)
            }
            guiy { GuildMemberManagementMenu(player) }
        }
    )
}

@Composable
fun DeclineGuildRequest(player: Player, modifier: Modifier, newMember: OfflinePlayer) {
    Item(
        TitleItem.of("${RED}Decline Join-Request"),
        modifier.size(3, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            newMember.removeGuildQueueEntries(GuildJoinType.Request)
            player.sendMessage("$YELLOW${BOLD}❌ ${YELLOW}You denied the join-request from ${newMember.name}")
            guiy { GuildMemberManagementMenu(player) }
        }
    )
}

@Composable
fun DeclineAllGuildRequests(player: Player, modifier: Modifier) {
    Item(
        TitleItem.of("${RED}Decline All Join-Request"),
        modifier.clickable {
            player.removeGuildQueueEntries(GuildJoinType.Request, true)
            player.sendMessage("$YELLOW${BOLD}❌ ${YELLOW}You denied all join-requests for your guild!")
            guiy { GuildMemberManagementMenu(player) }
        }
    )
}
