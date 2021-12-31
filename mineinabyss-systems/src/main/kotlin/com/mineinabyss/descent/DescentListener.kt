package com.mineinabyss.descent

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.components.descent.RemoveInOrth
import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.components.pins.OrthPins
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.mineinabyss.core.MIAConfig
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.time.Duration
import java.time.Instant
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

class DescentListener : Listener {
    @EventHandler
    fun PlayerAscendEvent.onReturnToOrth() {
        if (toSection != MIAConfig.data.hubSection) return
        player.removeDescentContext()
    }

    @EventHandler
    fun PlayerDescendEvent.onDescend() {
        if (fromSection != MIAConfig.data.hubSection) return
        player.toGeary {
            setPersisting(DescentContext())

            val addPins = get<OrthPins>()?.selected ?: setOf()
            set<ActivePins>(ActivePins(addPins.toMutableSet()))
        }
    }
}

@OptIn(ExperimentalTime::class)
fun Player.removeDescentContext() {
    val gearyPlayer = toGeary()
    val delve = gearyPlayer.get<DescentContext>() ?: return

    info(
        """
                &lWelcome back to Orth!
            &7Run lasted ${Duration.between(delve.startDate.toInstant(), Instant.now()).toKotlinDuration()}
            """.trimIndent().color()
    )
    gearyPlayer.remove<DescentContext>()
    gearyPlayer.removeRelations<RemoveInOrth>()
    gearyPlayer.get<ActivePins>()?.let {
        it.clear()
        gearyPlayer.remove<ActivePins>()
    }
}
