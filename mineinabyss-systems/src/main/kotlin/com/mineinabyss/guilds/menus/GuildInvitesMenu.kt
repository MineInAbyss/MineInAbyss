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
import com.mineinabyss.mineinabyss.extensions.*
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuiyOwner.GuildInvitesMenu(player: Player) {
    Chest(listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:guild_invites_menu:",
        4, onClose = { exit() }) {
        GuildInvites(player, Modifier.at(1,1))
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
            ( GuildJoinQueue.playerUUID eq player.uniqueId)
        }.map { row -> Pair(memberCount ,row[GuildJoinQueue.guildId])}

    }
    Grid(9, 4, modifier) {
        invites.sortedBy { it.first }.forEach { (memberCount, guild) ->
            val guildItem = ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Guildname: ${ChatColor.YELLOW}${ChatColor.ITALIC}${owner.getGuildName()}")
                lore = listOf("${ChatColor.BLUE}Click this to accept or deny invite.",
                    "${ChatColor.BLUE}Info about the guild can also be found in here."
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
        setDisplayName("${ChatColor.RED}Decline All Invites")
        //setCustomModelData(1)
    }, modifier.clickable {
        player.removeGuildQueueEntries(GuildJoinType.Invite, true)
        guiy { GuildMemberManagementMenu(player) }
        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}❌${ChatColor.YELLOW}You denied all invites!")


    })
}

@Composable
fun GuiyOwner.HandleGuildInvites(player: Player) {
    Chest(listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:handle_guild_invites:",
        5, onClose = { exit() }) {
        GuildLabel(player, Modifier.at(4,0))
        AcceptGuildInvite(player, Modifier.at(1,2))
        DeclineGuildInvite(player, Modifier.at(5,2))
        PreviousMenuButton(player, Modifier.at(4,4))
    }
}

@Composable
fun GuildLabel(player: Player, modifier: Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!

    Grid(2,2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
    }){
        Item(ItemStack(Material.PAPER).editItemMeta {
            setDisplayName(
                "${ChatColor.GOLD}${ChatColor.BOLD}Current Guild Info:"
            )
            lore = listOf(
                "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Name: ${ChatColor.YELLOW}${ChatColor.ITALIC}${guildOwner.getGuildName()}",
                "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Owner: ${ChatColor.YELLOW}${ChatColor.ITALIC}${guildOwner.name}",
                "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Level: ${ChatColor.YELLOW}${ChatColor.ITALIC}${guildOwner.getGuildLevel()}",
                "${ChatColor.YELLOW}${ChatColor.BOLD}Guild Members: ${ChatColor.YELLOW}${ChatColor.ITALIC}${guildOwner.getGuildMemberCount()}"
            )
        })
    }
}

@Composable
fun AcceptGuildInvite(player: Player, modifier: Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!

    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guildOwner.addMemberToGuild(player)
        if (guildOwner.getGuildMemberCount() >= guildOwner.getGuildLevel().times(5).plus(1)){
            player.error("This guild has reached its current member cap!")
        }
        player.removeGuildQueueEntries(GuildJoinType.Request)
        player.closeInventory()
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
fun DeclineGuildInvite(player: Player, modifier: Modifier) {
    val guildOwner = player.getGuildOwnerFromInvite().toPlayer()!!

    Grid(3, 2, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.removeGuildQueueEntries(GuildJoinType.Invite)
        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}❌ ${ChatColor.YELLOW}You denied the invite from ${ChatColor.GOLD}${ChatColor.ITALIC}${guildOwner.getGuildName()}")
        guiy { GuildMainMenu(player) }
    }){
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setDisplayName("${ChatColor.RED}Decline Invite")
                //setCustomModelData(1)
            })
        }
    }
}
