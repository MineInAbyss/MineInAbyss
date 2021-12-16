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
import com.mineinabyss.mineinabyss.data.GuildJoinQueue
import com.mineinabyss.mineinabyss.data.GuildJoinType
import com.mineinabyss.mineinabyss.extensions.*
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuiyOwner.GuildInvitesMenu(player: Player) {
    Chest(listOf(player), "${Space.of(-18)}$WHITE:guild_invites_menu:",
        4, onClose = { exit() }) {
        GuildInvites(player, Modifier.at(1, 1))
        DenyAllInvites(player, Modifier.at(8, 3))
        PreviousMenuButton(player, Modifier.at(2, 3))
    }
}

@Composable
fun GuildInvites(player: Player, modifier: Modifier) {
    /* Transaction to query GuildInvites and playerUUID */
    val owner = player.getGuildOwnerFromInvite().toPlayer()!!
    val memberCount = owner.getGuildMemberCount()
    val invites = transaction {
        GuildJoinQueue.select {
            (GuildJoinQueue.joinType eq GuildJoinType.Invite) and
                    (GuildJoinQueue.playerUUID eq player.uniqueId)
        }.map { row -> Pair(memberCount, row[GuildJoinQueue.guildId]) }

    }
    Grid(modifier.size(9, 4)) {
        invites.sortedBy { it.first }.forEach { (memberCount, guild) ->
            val guildItem = ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("$GOLD${BOLD}Guildname: $YELLOW$ITALIC${owner.getGuildName()}")
                lore = listOf(
                    "${BLUE}Click this to accept or deny invite.",
                    "${BLUE}Info about the guild can also be found in here."
                )
            }
            Item(guildItem, Modifier.clickable {
                player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
                guiy { HandleGuildInvites(player) }
            })
        }
    }
}

@Composable
fun DenyAllInvites(player: Player, modifier: Modifier) {
    Item(ItemStack(Material.PAPER).editItemMeta {
        setDisplayName("${RED}Decline All Invites")
        //setCustomModelData(1)
    }, modifier.clickable {
        player.removeGuildQueueEntries(GuildJoinType.Invite, true)
        guiy { GuildMemberManagementMenu(player) }
        player.sendMessage("$YELLOW${BOLD}❌${YELLOW}You denied all invites!")


    })
}

@Composable
fun GuiyOwner.HandleGuildInvites(player: Player) {
    Chest(listOf(player), "${Space.of(-18)}$WHITE:handle_guild_invites:",
        5, onClose = { exit() }) {
        GuildLabel(player, Modifier.at(4, 0))
        AcceptGuildInvite(player, Modifier.at(1, 2))
        DeclineGuildInvite(player, Modifier.at(5, 2))
        PreviousMenuButton(player, Modifier.at(4, 4))
    }
}

@Composable
fun GuildLabel(player: Player, modifier: Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!

    Item(
        TitleItem.of(
            "$GOLD${BOLD}Current Guild Info:",
            "$YELLOW${BOLD}Guild Name: $YELLOW$ITALIC${guildOwner.getGuildName()}",
            "$YELLOW${BOLD}Guild Owner: $YELLOW$ITALIC${guildOwner.name}",
            "$YELLOW${BOLD}Guild Level: $YELLOW$ITALIC${guildOwner.getGuildLevel()}",
            "$YELLOW${BOLD}Guild Members: $YELLOW$ITALIC${guildOwner.getGuildMemberCount()}"
        ),
        modifier.size(2, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        }
    )
}

@Composable
fun AcceptGuildInvite(player: Player, modifier: Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!

    Item(
        TitleItem.of("${GREEN}Accept Invite"),
        modifier.size(3, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            if (guildOwner.getGuildJoinType() == GuildJoinType.Request) {
                player.error("This guild is in 'Request-only' mode.")
                player.error("Change it to 'Any' or 'Invite-only' mode to accept invites.")
                return@clickable
            }
            guildOwner.addMemberToGuild(player)
            if (guildOwner.getGuildMemberCount() >= guildOwner.getGuildLevel().times(5).plus(1)) {
                player.error("This guild has reached its current member cap!")
                return@clickable
            }
            player.removeGuildQueueEntries(GuildJoinType.Request)
            player.closeInventory()
        }
    )
}

@Composable
fun DeclineGuildInvite(player: Player, modifier: Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!

    Item(
        TitleItem.of("${RED}Decline Invite"),
        modifier.size(3, 2).clickable {
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            player.removeGuildQueueEntries(GuildJoinType.Invite)
            player.sendMessage("$YELLOW${BOLD}❌ ${YELLOW}You denied the invite from $GOLD$ITALIC${guildOwner.getGuildName()}")
            guiy { GuildMainMenu(player) }
        }
    )
}
