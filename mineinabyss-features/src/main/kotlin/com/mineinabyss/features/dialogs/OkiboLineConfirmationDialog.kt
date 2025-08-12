package com.mineinabyss.features.dialogs

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class OkiboLineConfirmationDialog(
    val station: String,
    val destination: String
) {
    fun showDialog(player: Player) {
        Dialog.create { builder ->
            builder.empty().type(
                DialogType.confirmation(
                    ActionButton.builder(Component.text("Go to $destination")).action(
                        DialogAction.customClick(
                            DialogHelpers.okiboMarkerConfirm,
                            BinaryTagHolder.binaryTagHolder("{current:$station,destination:$destination}")
                        )
                    ).build(),
                    ActionButton.builder(Component.text("Cancel")).action(
                        DialogAction.customClick(DialogHelpers.okiboMarkerConfirm, null)
                    )
                        .build()
                )
            )
        }
    }
}