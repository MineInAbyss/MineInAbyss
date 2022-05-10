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
import net.luckperms.api.LuckPermsProvider
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player


val luckPerms = LuckPermsProvider.get()

@Composable
fun GuiyOwner.PlayerProfile(viewer: Player, player: Player) {
    val isPatreon = player.toGeary().has<Patreon>()
    val titleName = player.name.toList().joinToString { ":player_profile_$it:" }.replace(", ", "")
    val ranks = DisplayRanks(player)

    Chest(setOf(viewer),
        "${Space.of(-12)}:player_profile${if (isPatreon) "_patreon:" else ":"}${Space.of(-178)}$titleName${Space.of(-42)}${ranks}",
        Modifier.height(4),
        onClose = { viewer.closeInventory() }) {
        PlayerHead(player, Modifier.at(0, 1))
        Column(Modifier.at(2, 0)) {
            //DisplayRanks(player)
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
            "<light_purple><b>${player.name}".miniMsg(),
            "<light_purple>Deaths: <aqua>${player.getStatistic(Statistic.DEATHS)}".miniMsg(),
            "<light_purple>Time played: <aqua>${player.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20 / 3600}h".miniMsg(),
            "<light_purple>Time since last death: <aqua>${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h".miniMsg(),
            isLarge = true
        ), modifier = modifier
    )
    Item(
        TitleItem.of(
            "<light_purple><b>${player.name}".miniMsg(),
            "<light_purple>Deaths: <aqua>${player.getStatistic(Statistic.DEATHS)}".miniMsg(),
            "<light_purple>Time played: <aqua>${player.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20 / 3600}h".miniMsg(),
            "<light_purple>Time since last death: <aqua>${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h".miniMsg(),
        ), modifier = modifier.at(1, 1)
    )
    Item(
        TitleItem.of(
            "<light_purple><b>${player.name}".miniMsg(),
            "<light_purple>Deaths: <aqua>${player.getStatistic(Statistic.DEATHS)}".miniMsg(),
            "<light_purple>Time played: <aqua>${player.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20 / 3600}h".miniMsg(),
            "<light_purple>Time since last death: <aqua>${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h".miniMsg()
        ), modifier = modifier.at(0, 2)
    )
    Item(
        TitleItem.of(
            "<light_purple><b>${player.name}".miniMsg(),
            "<light_purple>Deaths: <aqua>${player.getStatistic(Statistic.DEATHS)}".miniMsg(),
            "<light_purple>Time played: <aqua>${player.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20 / 3600}h".miniMsg(),
            "<light_purple>Time since last death: <aqua>${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h".miniMsg()
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
    Item(TitleItem.of("<#FFBB1C>${player.playerData.orthCoinsHeld} Orth Coins".miniMsg()))
}

@Composable
fun CloutTokenBalance(player: Player) {
    Item(TitleItem.of("<#7289DA>${player.playerData.cloutTokensHeld} Clout Tokens".miniMsg()))
}

@Composable
fun GuildButton(player: Player, viewer: Player) {
    Button(enabled = player.hasGuild() && !viewer.hasGuild(), onClick = {
        guiy { GuildScreen.GuildLookupMembers(player.getGuildName()) }
    }) {
        Text(
            "<gold><b><i>${player.getGuildName()}".miniMsg(),
            "<yellow><b>Guild Owner:</b> <yellow><i>${Bukkit.getOfflinePlayer(player.getGuildOwner()).name}".miniMsg(),
            "<yellow><b>Guild Level:</b> <yellow><i>${player.getGuildLevel()}".miniMsg(),
            "<yellow><b>Guild Members:</b> <yellow><i>${player.getGuildMemberCount()}".miniMsg()
        )
    }
}

@Composable
fun DiscordButton(player: Player) {
    val linked = player.getLinkedDiscordAccount()

    if (linked == null) {
        Item(TitleItem.of(
            "<b><#718AD6>${"${player.name}</b> <#718AD6>has not"}".miniMsg(),
            "<#718AD6>linked an account.".miniMsg()))
    } else Item(TitleItem.of("<b><#718AD6>${linked}".miniMsg()))
}

@Composable
fun DisplayRanks(player: Player): String {
    val group = player.getGroups().filter { sortedRanks.contains(it) }.sortedBy { sortedRanks[it] }.first()
    val patreon = player.getGroups().first { it.contains("patreon") || it.contains("supporter") }
    if (patreon.isNotEmpty()) {
        return "${Space.of(34)}:player_profile_rank_$group:${Space.of(-6)}:player_profile_rank_$patreon:"
    }
    return "${Space.of(34)}:player_profile_rank_$group:"
}

val sortedRanks =
    listOf(
        "admin",
        "seniordeveloper",
        "communitymanager",
        "seniorbuilder",
        "seniordesigner",
        "moderator",
        "developer",
        "buildlead",
        "builder",
        "designer",
        "helper",
        "juniordeveloper",
        "juniorbuilder",
        "juniordesigner"
    ).withIndex().associate { it.value to it.index }
