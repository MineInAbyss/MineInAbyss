package com.mineinabyss.features.guilds

import com.mineinabyss.eternalfortune.api.events.PlayerOpenGraveEvent
import com.mineinabyss.features.guilds.extensions.getGuildName
import com.mineinabyss.features.guilds.extensions.hasGuild
import com.mineinabyss.features.helpers.di.Features
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EternalFortuneGuildListener : Listener {
    @EventHandler
    fun PlayerOpenGraveEvent.onOpenGuildMemberGrave() {
        if (grave.isExpired() || !grave.isProtected() || graveOwner.uniqueId == player.uniqueId) return

        isCancelled = when {
            !player.hasGuild() || !graveOwner.hasGuild() -> true
            player.getGuildName() != graveOwner.getGuildName() -> true
            else -> !Features.guilds.config.canOpenGuildMemberGraves
        }
    }
}
