package com.mineinabyss.features.playerprofile

import androidx.compose.runtime.*
import com.mineinabyss.components.PlayerData
import com.mineinabyss.components.playerprofile.PlayerProfile
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.menus.GuildScreen
import com.mineinabyss.features.helpers.*
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.guiy.layout.Column
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.idofront.nms.nbt.editOfflinePDC
import com.mineinabyss.idofront.nms.nbt.getOfflinePDC
import com.mineinabyss.idofront.nms.nbt.saveOfflinePDC
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.text.buildString

private inline fun <reified T : Any> OfflinePlayer.hasComponent(): Boolean {
    return if (isOnline) player!!.toGeary().has<T>()
    else with(abyss.gearyGlobal) { getOfflinePDC()?.has<T>() == true }
}

private inline fun <reified T : Any> OfflinePlayer.getComponent(): T? {
    return if (isOnline) player!!.toGeary().get<T>()
    else with(abyss.gearyGlobal) { getOfflinePDC()?.decode<T>() }
}

private inline fun <reified T : Any> OfflinePlayer.getOrSetComponent(default: T): T {
    return if (isOnline) player!!.toGeary().getOrSetPersisting<T> { default }
    else {
        val pdc = getOfflinePDC() ?: return default
        val t = with(abyss.gearyGlobal) { pdc.decode<T>() ?: run { pdc.encode(default); default } }
        saveOfflinePDC(pdc)
        return t
    }
}

private inline fun <reified T : Any> OfflinePlayer.setComponent(component: T) {
    if (isOnline) player!!.toGeary().setPersisting(component)
    else editOfflinePDC {
        with(abyss.gearyGlobal) { encode(component) }
    }
}

@Composable
fun PlayerProfile(viewer: Player, player: OfflinePlayer) {
    var hideArmorIcons by remember { mutableStateOf(player.getOrSetComponent<PlayerProfile>(PlayerProfile()).displayProfileArmor) }
    val isPatreon by remember { mutableStateOf(player.hasComponent<Patreon>()) }
    val backgroundId by remember { mutableStateOf(player.getOrSetComponent<PlayerProfile>(PlayerProfile(abyss.config.playerProfile.validBackgroundIds.firstOrNull() ?: "")).background) }
    val titleComponent = Component.text(buildString {
        append(":space_-8::player_profile")
        if (isPatreon) append("_patreon")
        append("_armor_")
        if (hideArmorIcons) append("hidden:") else append("visible:")
        append(":space_-172:")
    })
    val background = Component.text(":${backgroundId}:")
    val titleName = Component.text(":space_-92:${player.name}", NamedTextColor.WHITE)
    val rankComponent = Component.text(":survival::space_-40::${DisplayRanks(player)}")

    Chest(setOf(viewer),
        Component.textOfChildren(titleComponent, background, titleName, rankComponent),
        Modifier.height(4),
        onClose = { viewer.closeInventory() }) {
        PlayerHead(player, Modifier.at(0, 1))
        if (player == viewer) ToggleArmorVisibility {
            if (player == viewer) {
                player.setComponent(player.getComponent<PlayerProfile>()!!.copy(displayProfileArmor = !hideArmorIcons))
                hideArmorIcons = !hideArmorIcons
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

        val helmet = player.player?.equipment?.helmet.takeUnless { hideArmorIcons }
        val chestPlate = player.player?.equipment?.chestplate.takeUnless { hideArmorIcons }
        val leggings = player.player?.equipment?.leggings.takeUnless { hideArmorIcons }
        val boots = player.player?.equipment?.boots.takeUnless { hideArmorIcons }
        Column(Modifier.at(8, 0)) {
            Item(helmet)
            Item(chestPlate)
            Item(leggings)
            Item(boots)
        }
    }
}

@Composable
fun PlayerHead(player: OfflinePlayer, modifier: Modifier) {
    Item(
        TitleItem.head(
            player, "<light_purple><b>${player.name}".miniMsg(),
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
fun CosmeticHat(player: OfflinePlayer) =
    if (abyss.isHMCCosmeticsEnabled) player.player?.getCosmeticHat()?.item  ?: ItemStack(Material.AIR)
    else ItemStack(Material.AIR)

@Composable
fun CosmeticBackpack(player: OfflinePlayer) =
    if (abyss.isHMCCosmeticsEnabled) player.player?.getCosmeticBackpack()?.item ?: ItemStack(Material.AIR)
    else ItemStack(Material.AIR)

@Composable
fun OrthCoinBalance(player: OfflinePlayer) {
    val amount = player.getComponent<PlayerData>()?.orthCoinsHeld
    Item(TitleItem.of("<#FFBB1C>${amount} <b>Orth Coin${if (amount != 1) "s" else ""}".miniMsg()))
}

@Composable
fun MittyTokenBalance(player: OfflinePlayer) {
    val amount = player.getComponent<PlayerData>()?.mittyTokensHeld
    Item(TitleItem.of("<#b74b4d>${amount} <b>Mitty Token${if (amount != 1) "s" else ""}".miniMsg()))
}

@Composable
fun GuildButton(player: OfflinePlayer, viewer: Player) {
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
fun DiscordButton(player: OfflinePlayer) {
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
fun DisplayRanks(player: OfflinePlayer): String {
    var group = player.luckpermGroups.filter { it in sortedRanks }.sortedBy { sortedRanks[it] }.firstOrNull()
    val patreon = player.luckpermGroups.firstOrNull { "patreon" in it || "supporter" in it } ?: ""
    group = ":space_34:" + (group?.let { ":player_profile_rank_$group:" } ?: "")
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
