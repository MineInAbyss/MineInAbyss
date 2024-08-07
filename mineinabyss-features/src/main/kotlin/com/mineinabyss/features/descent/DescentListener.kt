package com.mineinabyss.features.descent

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.time.Duration
import java.time.Instant
import kotlin.time.toKotlinDuration

class DescentListener : Listener {
    @EventHandler
    fun PlayerAscendEvent.onReturnToOrth() {
        if (toSection != Features.layers.config.hubSection) return
        player.removeDescentContext()
    }

    @EventHandler
    fun PlayerDescendEvent.onDescend() {
        if (fromSection != Features.layers.config.hubSection) return
        player.toGeary().apply {
            setPersisting(DescentContext())
        }
    }
}

fun Player.removeDescentContext() {
    val gearyPlayer = toGeary()
    val delve = gearyPlayer.get<DescentContext>() ?: return

    info(
        """
                <bold>Welcome back to Orth!
            <gray>Run lasted ${Duration.between(delve.startDate.toInstant(), Instant.now()).toKotlinDuration()}
            """.trimIndent().miniMsg()
    )
    gearyPlayer.remove<DescentContext>()
}
