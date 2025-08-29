package com.mineinabyss.features.guilds.extensions

import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback
import net.kyori.adventure.audience.Audience

class GuildLookDialogCallback(val onClick: (String) -> Unit) : DialogActionCallback {
    override fun accept(response: DialogResponseView, audience: Audience) {
        onClick.invoke(response.getText("guild_dialog") ?: return)
    }
}