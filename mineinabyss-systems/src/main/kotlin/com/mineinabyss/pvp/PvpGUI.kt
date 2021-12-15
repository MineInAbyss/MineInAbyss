package com.mineinabyss.pvp

import androidx.compose.runtime.*
import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.pvp.ToggleIcon.disabled
import com.mineinabyss.pvp.ToggleIcon.enabled
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.PvpPrompt(player: Player) {
    Chest(listOf(player), "${Space.of(-18)}${ChatColor.WHITE}:pvp_menu_toggle:",
        4, onClose = { reopen() }) {
        EnablePvp(player, Modifier.at(1, 1))
        DisablePvp(player, Modifier.at(5, 1))
        TogglePvpPrompt(player, Modifier.at(8, 3))
    }
}

@Composable
fun EnablePvp(player: Player, modifier: Modifier) {
    val data = player.playerData
    Grid(3, 2, modifier.clickable {
        data.pvpStatus = true
        data.pvpUndecided = false
        player.success("PvP has been enabled!")
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
        player.closeInventory()
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.DARK_GREEN}${ChatColor.BOLD}Enable PvP")
                lore = mutableListOf("${ChatColor.GREEN}Enables pvp interactions with other players in the Abyss.")
            })
        }
    }
}

@Composable
fun DisablePvp(player: Player, modifier: Modifier) {
    val data = player.playerData
    Grid(3, 2, modifier.clickable {
        data.pvpStatus = false
        data.pvpUndecided = false
        player.error("PvP has been disabled!")
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 0.1f)
        player.closeInventory()
    }) {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.DARK_RED}${ChatColor.BOLD}Disable PvP")
                lore = listOf("${ChatColor.RED}Disables pvp interactions with other players in the Abyss.")
            })
        }
    }
}

@Composable
fun TogglePvpPrompt(player: Player, modifier: Modifier) {
    val data = player.playerData
    var isEnabled by remember { mutableStateOf(data.showPvpPrompt) }
    val item = if (isEnabled) enabled else disabled
    Item(item, modifier.clickable {
        data.showPvpPrompt = !data.showPvpPrompt
        player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1f)
        isEnabled = data.showPvpPrompt
        player.success(
            "PvP-prompt has been ${
                if (data.showPvpPrompt) "${ChatColor.BOLD}enabled"
                else "${ChatColor.BOLD}disabled"
            }."
        )
    })
}

object ToggleIcon {
    val enabled = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(2)
        setDisplayName("${ChatColor.BLUE}${ChatColor.BOLD}Toggle PvP Prompt")
        lore = listOf(
            "${ChatColor.RED}Disable ${ChatColor.DARK_AQUA}this prompt from showing " +
                    "when entering the Abyss. It can be re-opened at any time in Orth."
        )
    }

    val disabled = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(3)
        setDisplayName("${ChatColor.BLUE}${ChatColor.BOLD}Toggle PvP Prompt")
        lore = listOf(
            "${ChatColor.GREEN}Enable ${ChatColor.DARK_AQUA}this prompt from" +
                    " showing when entering the Abyss. It can be re-opened at any time in Orth."
        )
    }
}
