package com.mineinabyss.playerprofile

import androidx.compose.runtime.Composable
import com.mineinabyss.components.playerData
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guilds.menus.GuildScreen
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.helpers.*
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.Statistic
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.PlayerProfile(viewer: Player, player: Player) {
    val isPatreon = player.toGeary().has<Patreon>()
    val titleName = player.name.toList().joinToString { ":player_profile_$it:" }.replace(", ", "")
    val ranks = DisplayRanks(player)

    Chest(setOf(viewer),
        "${Space.of(-12)}:player_profile${if (isPatreon) "_patreon:" else ":"}${Space.of(-178)}$titleName${ranks}",
        Modifier.height(4),
        onClose = { viewer.closeInventory() }) {
        PlayerHead(player, Modifier.at(0, 1))
        Column(Modifier.at(2, 0)) {
            DisplayRanks(player)
        }
        Column(Modifier.at(5, 0)) {
            OrthCoinBalance(player)
            if (isPatreon) CloutTokenBalance(player)
            GuildButton(player, viewer)
            DiscordButton(player)
        }
        Column(Modifier.at(7, 0)) {
            CosmeticHat(player)
            CosmeticBackpack(player)
        }
        Column(Modifier.at(8, 0)) {
            HelmetSlot(player)

            ChestplateSlot(player)
            LeggingsSlot(player)
            BootsSlot(player)
        }
    }
}

@Composable
fun PlayerHead(player: Player, modifier: Modifier) {
    Item(
        player.head(
            "${LIGHT_PURPLE}${BOLD}${player.name}",
            "${LIGHT_PURPLE}Deaths: ${AQUA}${player.getStatistic(Statistic.DEATHS)}",
            "${LIGHT_PURPLE}Time played: ${AQUA}${player.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20 / 3600}h",
            "${LIGHT_PURPLE}Time since last death: ${AQUA}${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h",
            isLarge = true
        ), modifier = modifier
    )
    Item(
        TitleItem.of(
            "${LIGHT_PURPLE}${BOLD}${player.name}",
            "${LIGHT_PURPLE}Deaths: ${AQUA}${player.getStatistic(Statistic.DEATHS)}",
            "${LIGHT_PURPLE}Time played: ${AQUA}${player.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20 / 3600}h",
            "${LIGHT_PURPLE}Time since last death: ${AQUA}${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h"
        ), modifier = modifier.at(1, 1)
    )
    Item(
        TitleItem.of(
            "${LIGHT_PURPLE}${BOLD}${player.name}",
            "${LIGHT_PURPLE}Deaths: ${AQUA}${player.getStatistic(Statistic.DEATHS)}",
            "${LIGHT_PURPLE}Time played: ${AQUA}${player.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20 / 3600}h",
            "${LIGHT_PURPLE}Time since last death: ${AQUA}${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h"
        ), modifier = modifier.at(0, 2)
    )
    Item(
        TitleItem.of(
            "${LIGHT_PURPLE}${BOLD}${player.name}",
            "${LIGHT_PURPLE}Deaths: ${AQUA}${player.getStatistic(Statistic.DEATHS)}",
            "${LIGHT_PURPLE}Time played: ${AQUA}${player.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20 / 3600}h",
            "${LIGHT_PURPLE}Time since last death: ${AQUA}${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h"
        ), modifier = modifier.at(1, 2)
    )
}

@Composable
fun HelmetSlot(player: Player) {
    Item(player.equipment.helmet)
}

@Composable
fun ChestplateSlot(player: Player) {
    Item(player.equipment.chestplate)
}

@Composable
fun LeggingsSlot(player: Player) {
    Item(player.equipment.leggings)
}

@Composable
fun BootsSlot(player: Player) {
    Item(player.equipment.boots)
}

@Composable
fun CosmeticHat(player: Player) {
    Item(player.getCosmeticHat())
}

@Composable
fun CosmeticBackpack(player: Player) {
    Item(player.getCosmeticBackpack())
}

@Composable
fun OrthCoinBalance(player: Player) {
    Item(TitleItem.ofComponent("<#FFBB1C>${player.playerData.orthCoinsHeld} Orth Coins".miniMsg()))
}

@Composable
fun CloutTokenBalance(player: Player) {
    Item(TitleItem.ofComponent("<#7289DA>${player.playerData.cloutTokensHeld} Clout Tokens".miniMsg()))
}

@Composable
fun GuildButton(player: Player, viewer: Player) {
    Button(enabled = player.hasGuild() && !viewer.hasGuild(), onClick = {
        guiy { GuildScreen.GuildLookupMembers(player.getGuildName()) }
    }) {
        Text(
            "${GOLD}${BOLD}${ITALIC}${player.getGuildName()}",
            "${YELLOW}${BOLD}Guild Owner: ${YELLOW}${ITALIC}${Bukkit.getOfflinePlayer(player.getGuildOwner()).name}",
            "${YELLOW}${BOLD}Guild Level: ${YELLOW}${ITALIC}${player.getGuildLevel()}",
            "${YELLOW}${BOLD}Guild Members: ${YELLOW}${ITALIC}${player.getGuildMemberCount()}"
        )
    }
}

@Composable
fun DiscordButton(player: Player) {
    val linked = player.getLinkedDiscordAccount() ?: "<b>${player.name}</b> has not linked an account."
    Item(TitleItem.ofComponent("<b><#718AD6>${linked}".miniMsg()))
}

@Composable
fun DisplayRanks(player: Player): String {
    var ranks = ""
    player.effectivePermissions.forEach { perm ->
        if (!perm.permission.startsWith("group.") || perm.permission == "group.default") return@forEach
        else ranks += ":player_profile_rank_${perm.permission.toString().removePrefix("group.")}:"
    }
    return ranks
}
