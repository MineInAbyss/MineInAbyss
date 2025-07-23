package com.mineinabyss.features.gondolas.pass

import com.mineinabyss.components.gondolas.Ticket
import kotlinx.serialization.Serializable

@Serializable
data class TicketConfig (
    val tickets: Map<String, Ticket> = mapOf()
)
