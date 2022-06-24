package com.mineinabyss.pvp

import androidx.compose.runtime.*
import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.*
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.pvp.ToggleIcon.disabled
import com.mineinabyss.pvp.ToggleIcon.enabled
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.PvpPrompt(player: Player) {
    Chest(setOf(player), "${Space.of(-12)}<white>:pvp_menu_toggle:", Modifier.height(4),
        onClose = { reopen() }) {
        EnablePvp(player, Modifier.at(1, 1))
        DisablePvp(player, Modifier.at(5, 1))
        TogglePvpPrompt(player, Modifier.at(8, 3))
    }
}

@Composable
fun EnablePvp(player: Player, modifier: Modifier) {
    val data = player.playerData
    Item(
        TitleItem.of(
            "<dark_green><b>Enable PvP".miniMsg(),
            "<green>Enables pvp interactions with".miniMsg(),
            "<green>other players in the Abyss.".miniMsg()
        ),
        modifier.size(3, 2).clickable {
            data.pvpStatus = true
            data.pvpUndecided = false
            player.success("PvP has been enabled!")
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            player.closeInventory()
        }
    )
}

@Composable
fun DisablePvp(player: Player, modifier: Modifier) {
    val data = player.playerData
    Item(
        TitleItem.of(
            "<dark_red><b>Disable PvP".miniMsg(),
            "<red>Disables pvp interactions with".miniMsg(),
            "<red>other players in the Abyss.".miniMsg()
        ),
        modifier.size(3, 2).clickable {
            data.pvpStatus = false
            data.pvpUndecided = false
            player.error("PvP has been disabled!")
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 0.1f)
            player.closeInventory()
        }
    )
}

@Composable
fun TogglePvpPrompt(player: Player, modifier: Modifier) {
    val data = player.playerData
    var isEnabled by remember { mutableStateOf(data.showPvpPrompt) }
    val item = if (isEnabled) enabled else disabled
    Button(
        modifier = modifier,
        onClick = {
            data.showPvpPrompt = !data.showPvpPrompt
            isEnabled = data.showPvpPrompt
            player.success(
                "PvP-prompt has been ${
                    if (data.showPvpPrompt) "<b>enabled"
                    else "<b>disabled"
                }."
            )
        }
    ) { Item(item) }
}

object ToggleIcon {
    val enabled = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(2)
        displayName("<blue><b>Toggle PvP Prompt".miniMsg())
        lore(
            listOf(
                "<red>Disable <dark_aqua>this prompt from showing".miniMsg(),
                "<dark_aqua>when entering the <green>Abyss.".miniMsg(),
                "<dark_aqua>It can be re-opened at any time in <gold>Orth.".miniMsg()
            )
        )
    }

    val disabled =
        ItemStack(Material.PAPER).editItemMeta {
            setCustomModelData(3)
            displayName("<blue><b>Toggle PvP Prompt".miniMsg())
            lore(listOf(
                "<green>Enable <dark_aqua>this prompt from showing".miniMsg(),
                "<dark_aqua>when entering the <green>Abyss.".miniMsg(),
                "<dark_aqua>It can be re-opened at any time in <gold>Orth.".miniMsg()
            ))
        }
}
