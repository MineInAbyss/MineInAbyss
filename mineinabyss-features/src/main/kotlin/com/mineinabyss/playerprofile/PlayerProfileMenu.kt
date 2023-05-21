package com.mineinabyss.playerprofile

import androidx.compose.runtime.*
import com.mineinabyss.components.playerData
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
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
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.abyss
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.PlayerProfile(viewer: Player, player: Player) {
    var hideArmorIcons by remember { mutableStateOf(player.playerData.displayProfileArmor) }
    val isPatreon = player.toGeary().has<Patreon>()
    val titleName = Component.text(player.name).font(Key.key("playerprofile")).color(TextColor.color(0xFFFFFF))
    val titleComponent =
        Component.text(
            "${Space.of(-12)}:player_profile${if (isPatreon) "_patreon" else ""}${if (!hideArmorIcons) "_armor_hidden:" else "_armor_visible:"}${
                Space.of(
                    -178
                )
            }"
        )
    val rankComponent = Component.text("${Space.of(-42)}${DisplayRanks(player)}")

    Chest(setOf(viewer),
        titleComponent.append(titleName).append(rankComponent),
        Modifier.height(4),
        onClose = { viewer.closeInventory() }) {
        PlayerHead(player, Modifier.at(0, 1))
        ToggleArmorVisibility {
            if (player == viewer) {
                player.playerData.displayProfileArmor = !hideArmorIcons
                hideArmorIcons = player.playerData.displayProfileArmor
            }
        }
        Column(Modifier.at(5, 0)) {
            OrthCoinBalance(player)
            if (isPatreon) MittyTokenBalance(player)
            GuildButton(player, viewer)
            DiscordButton(player)
        }
        Column(Modifier.at(7, 0)) {
            CosmeticHat(player)
            CosmeticBackpack(player)
        }
        Column(Modifier.at(8, 0)) {
            HelmetSlot(player, hideArmorIcons)
            ChestplateSlot(player, hideArmorIcons)
            LeggingsSlot(player, hideArmorIcons)
            BootsSlot(player, hideArmorIcons)
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
fun HelmetSlot(player: Player, hideArmorIcons: Boolean) =
    if (hideArmorIcons) Item(player.equipment.helmet) else Item(null)


@Composable
fun ChestplateSlot(player: Player, hideArmorIcons: Boolean) =
    if (hideArmorIcons) Item(player.equipment.chestplate) else Item(null)


@Composable
fun LeggingsSlot(player: Player, hideArmorIcons: Boolean) =
    if (hideArmorIcons) Item(player.equipment.leggings) else Item(null)


@Composable
fun BootsSlot(player: Player, hideArmorIcons: Boolean) =
    if (hideArmorIcons) Item(player.equipment.boots) else Item(null)


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
        guiy { GuildScreen.GuildLookupMembers(player.getGuildName()) }
    }) {
        if (player.hasGuild()) {
            Text(
                "<gold><b><i>${player.getGuildName()}".miniMsg(),
                "<yellow><b>Guild Owner:</b> <yellow><i>${Bukkit.getOfflinePlayer(player.getGuildOwner()).name}".miniMsg(),
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
    var group = player.luckpermGroups.filter { sortedRanks.contains(it) }.sortedBy { sortedRanks[it] }.firstOrNull()
    val patreon = player.luckpermGroups.firstOrNull { "patreon" in it || "supporter" in it }
    group = if (group != null) "${Space.of(34)}:player_profile_rank_$group:" else Space.of(34)
    if (!patreon.isNullOrBlank()) group += ":${Space.of(-6)}:player_profile_rank_$patreon:"

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
