package com.mineinabyss.descent

import com.mineinabyss.components.descent.DescentContext
import com.mineinabyss.components.pins.ActivePins
import com.mineinabyss.components.pins.OrthPins
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.components.RelationComponent
import com.mineinabyss.geary.ecs.helpers.GearyKoinComponent
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.mineinabyss.core.MIAConfig
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.time.Duration
import java.time.Instant
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

fun Player.removeDescentContext(): Unit = GearyKoinComponent().run {
    val gearyPlayer = toGeary()
    val delve = gearyPlayer.get<DescentContext>() ?: return

    info(
        """
                &lWelcome back to Orth!
            &7Run lasted ${Duration.between(delve.startDate.toInstant(), Instant.now()).toKotlinDuration()}
            """.trimIndent().color()
    )
    gearyPlayer.remove<DescentContext>()

    //TODO replace this with new syntax in geary once it comes around

    val comps = engine.getRelationsFor(gearyPlayer, RelationValueId(componentId<RelationComponent>()))
    comps.forEach { (_, relation) ->
        gearyPlayer.removeRelation(relation)
    }
    gearyPlayer.get<ActivePins>()?.let {
        it.clear()
        gearyPlayer.remove<ActivePins>()
    }
}
