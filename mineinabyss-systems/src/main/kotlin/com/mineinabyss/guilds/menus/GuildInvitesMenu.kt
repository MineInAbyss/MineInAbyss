package com.mineinabyss.guilds.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.nodes.InventoryCanvasScope.at
import com.mineinabyss.idofront.font.NegativeSpace
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.mineinabyss.data.GuildJoinQueue
import com.mineinabyss.mineinabyss.data.Guilds
import com.mineinabyss.mineinabyss.data.Players
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
fun GuiyOwner.GuildInvitesMenu(player: Player) {
    Chest(listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:guild_invites_menu:",
        4, onClose = { exit() }) {
        GuildInviteMenu(player, Modifier.at(1,1))
    }
}

@Composable
fun GuildInviteMenu(player: Player, modifier: Modifier) {
    /* Implement transaction to query GuildInvites and playerUUID */
    transaction {

        val guildId = GuildJoinQueue.select {
            (GuildJoinQueue.playerUUID eq player.uniqueId) and (GuildJoinQueue.guildId eq Guilds.id)
        }.single()[GuildJoinQueue.guildId]

        val memberGuildCheck = Players.select {
            Players.playerUUID eq player.uniqueId
        }.firstOrNull()?.get(Players.guildId)


    }
    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        guiy { HandleGuildInvites(player) }
    }){
        ItemStack(Material.PAPER).editItemMeta {
            setDisplayName("Guildname")
            setCustomModelData(1)
        }
    }
}

@Composable
fun GuiyOwner.HandleGuildInvites(player: Player) {
    Chest(listOf(player), "${NegativeSpace.of(18)}${ChatColor.WHITE}:handle_guild_invites:",
        4, onClose = { exit() }) {
        //GuildInvites(player, Modifier.at(1,1))
    }
    AcceptGuildInvite(player, Modifier.at(2,2))
    DeclineGuildInvite(player, Modifier.at(3,2))
}

@Composable
fun AcceptGuildInvite(player: Player, modifier: Modifier) {
    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        /* Add player to guild and remove invite from table */
    }){
        ItemStack(Material.PAPER).editItemMeta {
            setDisplayName("${ChatColor.GREEN}Accept Invite")
            setCustomModelData(1)
        }
    }
}

@Composable
fun DeclineGuildInvite(player: Player, modifier: Modifier) {
    Grid(1, 1, modifier.clickable {
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        /* Remove invite from table */
    }){
        ItemStack(Material.PAPER).editItemMeta {
            setDisplayName("${ChatColor.RED}Decline Invite")
            setCustomModelData(1)
        }
    }
}
