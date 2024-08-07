package com.mineinabyss.features.playerprofile

import androidx.compose.runtime.*
import com.mineinabyss.components.playerData
import com.mineinabyss.components.playerprofile.PlayerProfile
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.menus.GuildScreen
import com.mineinabyss.features.helpers.*
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun PlayerProfile(viewer: Player, player: Player) {
    var hideArmorIcons by remember { mutableStateOf(player.playerData.displayProfileArmor) }
    val isPatreon by remember { mutableStateOf(player.toGeary().has<Patreon>()) }
    val backgroundId by remember { mutableStateOf(player.toGeary().getOrSetPersisting<PlayerProfile> { PlayerProfile() }.background) }
    val titleComponent = Component.text(":space_-8::player_profile" +
            (if (isPatreon) "_patreon" else "") +
            ("_armor_" + if (!hideArmorIcons) "hidden:" else "visible:") +
            ":space_-172:")
    val background = Component.text(":${backgroundId}:")
    val titleName = Component.text(":space_-92:${player.name}", NamedTextColor.WHITE)
    val rankComponent = Component.text(":survival::space_-40::${DisplayRanks(player)}")

    Chest(setOf(viewer),
        Component.textOfChildren(titleComponent, background, titleName, rankComponent),
        Modifier.height(4),
        onClose = { viewer.closeInventory() }) {
        PlayerHead(player, Modifier.at(0, 1))
        ToggleArmorVisibility {
            if (player == viewer) {
                player.playerData.displayProfileArmor = !hideArmorIcons
                hideArmorIcons = !hideArmorIcons
            }
        }
        Column(Modifier.at(5, 0)) {
            OrthCoinBalance(player)
            if (!isPatreon) MittyTokenBalance(player)
            GuildButton(player, viewer)
            DiscordButton(player)
        }
        Column(Modifier.at(7, 0)) {
            CosmeticHat(player)
            CosmeticBackpack(player)
        }

        val helmet = player.equipment.helmet.takeUnless { hideArmorIcons }
        val chestPlate = player.equipment.chestplate.takeUnless { hideArmorIcons }
        val leggings = player.equipment.leggings.takeUnless { hideArmorIcons }
        val boots = player.equipment.boots.takeUnless { hideArmorIcons }
        Column(Modifier.at(8, 0)) {
            Item(helmet)
            Item(chestPlate)
            Item(leggings)
            Item(boots)
        }
    }
}

@Composable
fun PlayerHead(player: Player, modifier: Modifier) {
    Item(
        player.head(
            "<light_purple><b>${player.name}".miniMsg(),
            "<light_purple>Deaths: <aqua>${player.getStatistic(Statistic.DEATHS)}".miniMsg(),
            "<light_purple>Time played: <aqua>${player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 3600}h".miniMsg(),
            "<light_purple>Time since last death: <aqua>${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h".miniMsg(),
            isLarge = true
        ), modifier = modifier
    )
    Item(
        TitleItem.of(
            "<light_purple><b>${player.name}".miniMsg(),
            "<light_purple>Deaths: <aqua>${player.getStatistic(Statistic.DEATHS)}".miniMsg(),
            "<light_purple>Time played: <aqua>${player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 3600}h".miniMsg(),
            "<light_purple>Time since last death: <aqua>${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h".miniMsg(),
        ), modifier = modifier.at(1, 1)
    )
    Item(
        TitleItem.of(
            "<light_purple><b>${player.name}".miniMsg(),
            "<light_purple>Deaths: <aqua>${player.getStatistic(Statistic.DEATHS)}".miniMsg(),
            "<light_purple>Time played: <aqua>${player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 3600}h".miniMsg(),
            "<light_purple>Time since last death: <aqua>${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h".miniMsg()
        ), modifier = modifier.at(0, 2)
    )
    Item(
        TitleItem.of(
            "<light_purple><b>${player.name}".miniMsg(),
            "<light_purple>Deaths: <aqua>${player.getStatistic(Statistic.DEATHS)}".miniMsg(),
            "<light_purple>Time played: <aqua>${player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 3600}h".miniMsg(),
            "<light_purple>Time since last death: <aqua>${player.getStatistic(Statistic.TIME_SINCE_DEATH) / 20 / 3600}h".miniMsg()
        ), modifier = modifier.at(1, 2)
    )
}


@Composable
fun ToggleArmorVisibility(toggleArmor: () -> Unit) {
    Button(onClick = toggleArmor, modifier = Modifier.at(7, 3)) {
        Text(
            "<b><dark_purple>Toggle armor visibility".miniMsg(),
            "<light_purple>Hides your armor from other".miniMsg(),
            "<light_purple>players viewing your profile".miniMsg()
        )
    }
}

@Composable
fun CosmeticHat(player: Player) =
    if (abyss.isHMCCosmeticsEnabled) player.getCosmeticHat()?.item  ?: ItemStack(Material.AIR)
    else ItemStack(Material.AIR)

@Composable
fun CosmeticBackpack(player: Player) =
    if (abyss.isHMCCosmeticsEnabled) player.getCosmeticBackpack()?.item ?: ItemStack(Material.AIR)
    else ItemStack(Material.AIR)

@Composable
fun OrthCoinBalance(player: Player) {
    val amount = player.playerData.orthCoinsHeld
    Item(TitleItem.of("<#FFBB1C>${amount} <b>Orth Coin${if (amount != 1) "s" else ""}".miniMsg()))
}

@Composable
fun MittyTokenBalance(player: Player) {
    val amount = player.playerData.mittyTokensHeld
    Item(TitleItem.of("<#b74b4d>${amount} <b>Mitty Token${if (amount != 1) "s" else ""}".miniMsg()))
}

@Composable
fun GuildButton(player: Player, viewer: Player) {
    Button(enabled = player.hasGuild() && !viewer.hasGuild(), onClick = {
        guiy { player.getGuildName()?.let { GuildScreen.GuildLookupMembers(it) } }
    }) {
        if (player.hasGuild()) {
            Text(
                "<gold><b><i>${player.getGuildName()}".miniMsg(),
                "<yellow><b>Guild Owner:</b> <yellow><i>${Bukkit.getOfflinePlayer(player.getGuildOwner()!!).name}".miniMsg(),
                "<yellow><b>Guild Level:</b> <yellow><i>${player.getGuildLevel()}".miniMsg(),
                "<yellow><b>Guild Members:</b> <yellow><i>${player.getGuildMemberCount()}".miniMsg()
            )
        } else Text("<gold><b><i>${player.name}</b> is not".miniMsg(), "<gold><i>in any guild.".miniMsg())
    }
}

@Composable
fun DiscordButton(player: Player) {
    val linked = player.linkedDiscordAccount

    if (linked == null) {
        Item(
            TitleItem.of(
                "<b><#718AD6>${"${player.name}</b> <#718AD6>has not"}".miniMsg(),
                "<#718AD6>linked an account.".miniMsg()
            )
        )
    } else Item(TitleItem.of("<b><#718AD6>${player.name} is linked with <b>${linked}</b>.".miniMsg()))
}

@Composable
fun DisplayRanks(player: Player): String {
    var group = player.luckpermGroups.filter { it in sortedRanks }.sortedBy { sortedRanks[it] }.firstOrNull()
    val patreon = player.luckpermGroups.firstOrNull { "patreon" in it || "supporter" in it } ?: ""
    group = ":space_34:" + group?.let { ":player_profile_rank_$group:" }
    if (patreon.isNotEmpty()) group += ":space_-4::player_profile_rank_$patreon:"

    return group
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
