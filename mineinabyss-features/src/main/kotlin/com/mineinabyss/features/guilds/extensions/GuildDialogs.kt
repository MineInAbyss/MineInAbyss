package com.mineinabyss.features.guilds.extensions

import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.event.ClickCallback

class GuildDialogs(val title: String, val actionButtonLabel: String, val inputs: List<DialogInput>) {

    fun createGuildLookDialog(onClick: (String) -> Unit): Dialog {
        return Dialog.create { builder ->
            builder.empty().type(
                DialogType.notice(
                    ActionButton.builder(actionButtonLabel.miniMsg())
                        .action(
                            DialogAction.customClick(
                                GuildLookDialogCallback(onClick),
                                ClickCallback.Options.builder().build()
                            )
                        )
                        .build()
                ))
                .base(
                    DialogBase.builder(title.miniMsg())
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .pause(false)
                        .canCloseWithEscape(true)
                        .inputs(inputs)
                        .build()
                )
        }
    }
}