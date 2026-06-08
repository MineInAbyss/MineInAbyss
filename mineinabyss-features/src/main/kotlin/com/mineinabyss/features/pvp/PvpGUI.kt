package com.mineinabyss.features.pvp

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.features.pvp.ToggleIcon.disabled
import com.mineinabyss.features.pvp.ToggleIcon.enabled
import com.mineinabyss.guiy.canvas.LocalGuiyOwner
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun PvpPrompt(player: Player) {
    Chest(":space_-8::pvp_menu_toggle:", Modifier.height(4.dp), onClose = {}) {
        EnablePvp(player, Modifier.offset(1.dp, 1.dp))
        DisablePvp(player, Modifier.offset(5.dp, 1.dp))
        TogglePvpPrompt(player, Modifier.offset(8.dp, 3.dp))
    }
}

@Composable
fun EnablePvp(player: Player, modifier: Modifier) {
    val owner = LocalGuiyOwner.current
    Item(
        TitleItem.of(
            "<dark_green><b>Enable PvP".miniMsg(),
            "<green>Enables pvp interactions with".miniMsg(),
            "<green>other players in the Abyss.".miniMsg()
        ),
        modifier.size(3.dp, 2.dp).clickable {
            player.editPlayerData {
                pvpStatus = true
                pvpUndecided = false
            }
            player.success("PvP has been enabled!")
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
            owner.exit()
        }
    )
}

@Composable
fun DisablePvp(player: Player, modifier: Modifier) {
    val owner = LocalGuiyOwner.current
    Item(
        TitleItem.of(
            "<dark_red><b>Disable PvP".miniMsg(),
            "<red>Disables pvp interactions with".miniMsg(),
            "<red>other players in the Abyss.".miniMsg()
        ),
        modifier.size(3.dp, 2.dp).clickable {
            player.editPlayerData {
                pvpStatus = false
                pvpUndecided = false
            }
            player.error("PvP has been disabled!")
            player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 0.1f)
            owner.exit()
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
    private val pvpIconModel = Key.key("mineinabyss:pvp_toggle_icon")

    val enabled = ItemStack(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_MODEL, pvpIconModel)
        setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFlag(true).build())
        setData(DataComponentTypes.ITEM_NAME, "<blue><b>Toggle PvP Prompt".miniMsg())
        setData(DataComponentTypes.LORE, ItemLore.lore(listOf(
            "<red>Disable <dark_aqua>this prompt from showing".miniMsg(),
            "<dark_aqua>when entering the <green>Abyss.".miniMsg(),
            "<dark_aqua>It can be re-opened at any time in <gold>Orth.".miniMsg()
        )))
    }

    val disabled = ItemStack(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_MODEL, pvpIconModel)
        setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFlag(false).build())
        setData(DataComponentTypes.ITEM_NAME, "<blue><b>Toggle PvP Prompt".miniMsg())
        setData(DataComponentTypes.LORE, ItemLore.lore(listOf(
            "<green>Enable <dark_aqua>this prompt from showing".miniMsg(),
            "<dark_aqua>when entering the <green>Abyss.".miniMsg(),
            "<dark_aqua>It can be re-opened at any time in <gold>Orth.".miniMsg()
        )))
    }
}
