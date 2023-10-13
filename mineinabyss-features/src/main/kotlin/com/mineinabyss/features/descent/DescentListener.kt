package com.mineinabyss.features.descent

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.components.pins.OrthPins
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.abyss
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.time.Duration
import java.time.Instant
import kotlin.time.toKotlinDuration

class DescentListener : Listener {
    @EventHandler
    fun PlayerAscendEvent.onReturnToOrth() {
        if (toSection != Features.layers.hubSection) return
        player.removeDescentContext()
    }

    @EventHandler
    fun PlayerDescendEvent.onDescend() {
        if (fromSection != Features.layers.hubSection) return
        player.toGeary().apply {
            setPersisting(DescentContext())

            val addPins = get<OrthPins>()?.selected ?: setOf()
            set<ActivePins>(ActivePins(addPins.toMutableSet()))
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

    gearyPlayer.get<ActivePins>()?.let {
        it.clear()
        gearyPlayer.remove<ActivePins>()
    }
}
