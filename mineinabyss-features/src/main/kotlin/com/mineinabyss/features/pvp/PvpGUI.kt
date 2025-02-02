package com.mineinabyss.features.pvp

import androidx.compose.runtime.*
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.playerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.features.pvp.ToggleIcon.disabled
import com.mineinabyss.features.pvp.ToggleIcon.enabled
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun PvpPrompt(player: Player) {
    Chest(setOf(player), ":space_-8::pvp_menu_toggle:", Modifier.height(4),
        onClose = { reopen() }) {
        EnablePvp(player, Modifier.at(1, 1))
        DisablePvp(player, Modifier.at(5, 1))
        TogglePvpPrompt(player, Modifier.at(8, 3))
    }
}

@Composable
fun EnablePvp(player: Player, modifier: Modifier) {
    Item(
        TitleItem.of(
            "<dark_green><b>Enable PvP".miniMsg(),
            "<green>Enables pvp interactions with".miniMsg(),
            "<green>other players in the Abyss.".miniMsg()
        ),
        modifier.size(3, 2).clickable {
            player.editPlayerData {
                pvpStatus = true
                pvpUndecided = false
            }
            player.success("PvP has been enabled!")
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            player.closeInventory()
        }
    )
}

@Composable
fun DisablePvp(player: Player, modifier: Modifier) {
    Item(
        TitleItem.of(
            "<dark_red><b>Disable PvP".miniMsg(),
            "<red>Disables pvp interactions with".miniMsg(),
            "<red>other players in the Abyss.".miniMsg()
        ),
        modifier.size(3, 2).clickable {
            player.editPlayerData {
                pvpStatus = false
                pvpUndecided = false
            }
            player.error("PvP has been disabled!")
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 0.1f)
            player.closeInventory()
        }
    )
}

@Composable
fun TogglePvpPrompt(player: Player, modifier: Modifier) {
    val data = player.playerDataOrNull
    //TODO replace with get component as flow
    var isEnabled by remember { mutableStateOf(data?.showPvpPrompt ?: true) }

    Button(
        modifier = modifier,
        onClick = {
            isEnabled = player.editPlayerData {
                showPvpPrompt = !showPvpPrompt
                showPvpPrompt
            }
            player.success("PvP-prompt has been ${if (data?.showPvpPrompt != false) "<b>enabled" else "<b>disabled"}.")
        }
    ) { Item(if (isEnabled) enabled else disabled) }
}

object ToggleIcon {
    val enabled = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(2)
        itemName("<blue><b>Toggle PvP Prompt".miniMsg())
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
            itemName("<blue><b>Toggle PvP Prompt".miniMsg())
            lore(
                listOf(
                    "<green>Enable <dark_aqua>this prompt from showing".miniMsg(),
                    "<dark_aqua>when entering the <green>Abyss.".miniMsg(),
                    "<dark_aqua>It can be re-opened at any time in <gold>Orth.".miniMsg()
                )
            )
        }
}
