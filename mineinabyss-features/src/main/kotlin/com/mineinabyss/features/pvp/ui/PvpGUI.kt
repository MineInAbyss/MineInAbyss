package com.mineinabyss.features.pvp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.PlayerDataViewModel
import com.mineinabyss.features.pvp.ui.ToggleIcon.disabled
import com.mineinabyss.features.pvp.ui.ToggleIcon.enabled
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.inventory.CurrentPlayer
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.viewmodel.viewModel
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import org.bukkit.Sound
import org.bukkit.entity.Player

@Composable
fun PvpPrompt(player: Player = CurrentPlayer) {
    viewModel { PlayerDataViewModel(player) }
    Chest(":space_-8::pvp_menu_toggle:", Modifier.height(4), onClose = {}) {
        EnablePvp(Modifier.at(1, 1))
        DisablePvp(Modifier.at(5, 1))
        TogglePvpPrompt(Modifier.at(8, 3))
    }
}

@Composable
fun EnablePvp(
    modifier: Modifier,
    player: Player = CurrentPlayer,
) = Text(
    "<dark_green><b>Enable PvP",
    "<green>Enables pvp interactions with",
    "<green>other players in the Abyss.",
    modifier = modifier.size(3, 2).clickable {
        player.editPlayerData {
            pvpStatus = true
            pvpUndecided = false
        }
        player.success("PvP has been enabled!")
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.closeInventory()
    }
)

@Composable
fun DisablePvp(modifier: Modifier, player: Player = CurrentPlayer) = Text(
    "<dark_red><b>Disable PvP",
    "<red>Disables pvp interactions with",
    "<red>other players in the Abyss.",
    modifier = modifier.size(3, 2).clickable {
        player.editPlayerData {
            pvpStatus = false
            pvpUndecided = false
        }
        player.error("PvP has been disabled!")
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 0.1f)
        player.closeInventory()
    }
)

@Composable
fun TogglePvpPrompt(
    modifier: Modifier,
    viewModel: PlayerDataViewModel = viewModel(),
) {
    val player = CurrentPlayer
    val data by viewModel.playerData.collectAsState()
    var isEnabled = data?.showPvpPrompt == true

    Button(
        modifier = modifier,
        onClick = {
            player.editPlayerData {
                showPvpPrompt = !showPvpPrompt
            }
            player.success("PvP-prompt has been ${if (data?.showPvpPrompt != false) "<b>enabled" else "<b>disabled"}.")
        }
    ) { Item(if (isEnabled) enabled else disabled) }
}

