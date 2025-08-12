package com.mineinabyss.features.dialogs

import com.mineinabyss.emojy.emojy
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class VerticalMapDialog() {
    fun showDialog(player: Player) {
        val dialog = Dialog.create { builder ->
            builder.empty().type(DialogType.notice()).base(
                DialogBase.builder(Component.text("<shift:1000>")).pause(false).canCloseWithEscape(true).body(listOf(
                    DialogBody.plainMessage(emojy.emotes.find { it.id == "vertical_map" }!!.formattedComponent().font(Key.key("nexo:default")), 1024)
                )).build()
            )
        }
        player.showDialog(dialog)
    }
}