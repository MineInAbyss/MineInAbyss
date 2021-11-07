package com.mineinabyss.pvp

import androidx.compose.runtime.*
import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.nodes.InventoryCanvasScope.at
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.NegativeSpace
import com.mineinabyss.pvp.ToggleIcon.disabled
import com.mineinabyss.pvp.ToggleIcon.enabled
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun GuiyOwner.PvpPrompt(player: Player) {
    val data = player.playerData
    Chest(listOf(player), NegativeSpace.MINUS_NINE + "${ChatColor.WHITE}:pvp_menu_toggle:",
        4, onClose = { reopen() }) {
        EnablePvp(player)
        DisablePvp(player)
        TogglePvpPrompt(player)
    }
}

@Composable
fun EnablePvp(player: Player) {
    val data = player.playerData
    Grid(3, 2, Modifier.at(1, 1).clickable
    {
        data.pvpStatus = true
        data.pvpUndecided = false
        player.success("PvP has been enabled!")
        player.playSound(player.location, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f)
        player.closeInventory()
    })
    {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.GREEN}${ChatColor.BOLD}Enable PvP")
            })
        }
    }
}

@Composable
fun DisablePvp(player: Player) {
    val data = player.playerData
    Grid(3, 2, Modifier.at(5, 1).clickable {
        data.pvpStatus = false
        data.pvpUndecided = false
        player.error("PvP has been disabled!")
        player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 1f)
        player.closeInventory()
    }) {
        repeat(6) {
            Item(ItemStack(Material.PAPER).editItemMeta {
                setCustomModelData(1)
                setDisplayName("${ChatColor.RED}${ChatColor.BOLD}Disable PvP")
            })
        }
    }
}

@Composable
fun TogglePvpPrompt(player: Player) {
    val data = player.playerData
    var isEnabled by remember { mutableStateOf(data.showPvpPrompt) }
    val item = if (isEnabled) enabled else disabled
    Item(item, Modifier.at(8, 3).clickable {
        player.playerData.showPvpPrompt = !player.playerData.showPvpPrompt
        isEnabled = player.playerData.showPvpPrompt
        player.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 1f)
        player.success(
            "PvP-prompt has been ${
                if (player.playerData.showPvpPrompt) "${ChatColor.BOLD}enabled"
                else "${ChatColor.BOLD}disabled"}."
        )
    })
}

object ToggleIcon {
    val enabled =
    ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(2)
        setDisplayName("${ChatColor.BLUE}${ChatColor.BOLD}Toggle PvP Prompt")
    }

    val disabled =
    ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(3)
        setDisplayName("${ChatColor.BLUE}${ChatColor.BOLD}Toggle PvP Prompt")
    }
}