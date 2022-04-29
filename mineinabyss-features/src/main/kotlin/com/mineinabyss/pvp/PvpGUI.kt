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
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.pvp.ToggleIcon.disabled
import com.mineinabyss.pvp.ToggleIcon.enabled
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.PvpPrompt(player: Player) {
    Chest(setOf(player), "${Space.of(-12)}$WHITE:pvp_menu_toggle:", Modifier.height(4),
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
            "$DARK_GREEN${BOLD}Enable PvP",
            "${GREEN}Enables pvp interactions with",
            "${GREEN}other players in the Abyss."
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
            "$DARK_RED${BOLD}Disable PvP",
            "${RED}Disables pvp interactions with",
            "${RED}other players in the Abyss."
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
                    if (data.showPvpPrompt) "${BOLD}enabled"
                    else "${BOLD}disabled"
                }."
            )
        }
    ) { Item(item) }
}

object ToggleIcon {
    val enabled = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(2)
        setDisplayName("$BLUE${BOLD}Toggle PvP Prompt")
        lore = listOf(
            "${RED}Disable ${DARK_AQUA}this prompt from showing",
            "${DARK_AQUA}when entering the ${GREEN}Abyss.",
            "${DARK_AQUA}It can be re-opened at any time in ${GOLD}Orth."
        )
    }

    val disabled =
        ItemStack(Material.PAPER).editItemMeta {
            setCustomModelData(3)
            setDisplayName("$BLUE${BOLD}Toggle PvP Prompt")
            lore = listOf(
                "${GREEN}Enable ${DARK_AQUA}this prompt from",
                "${DARK_AQUA}when entering the ${GREEN}Abyss.",
                "${DARK_AQUA}It can be re-opened at any time in ${GOLD}Orth."
            )
        }
}
