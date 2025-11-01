package com.mineinabyss.features.gondolas.pass

import com.mineinabyss.components.gondolas.Ticket
import com.mineinabyss.components.gondolas.UnlockedGondolas
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import org.bukkit.entity.Player
import com.mineinabyss.idofront.messaging.success

fun Player.unlockRoute(ticket: Ticket) {
    val unlocked =  this.toGeary().getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
    for (gondolasId in ticket.gondolasInRoute) {
        unlocked.keys.add(gondolasId)
    }
    this.success("Unlocked route of: ${ticket.ticketName}")
}

fun Player.removeRoute(gondolaId: String) {
    val ticketConfig = TicketConfigHolder.config ?: return;
    val unlocked =  this.toGeary().getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
    val ticket = ticketConfig.tickets.values.firstOrNull { gondolaId in it.gondolasInRoute && it.consumeWhenUsed} ?: return
    unlocked.keys -= ticket.gondolasInRoute.toSet()
    this.success("Removed route of: ${ticket.ticketName}")
}

fun Player.isRouteUnlocked(ticket: Ticket): Boolean {
    val unlocked =  this.toGeary().getOrSetPersisting<UnlockedGondolas> { UnlockedGondolas() }
    return ticket.gondolasInRoute.all { it in unlocked.keys }
}