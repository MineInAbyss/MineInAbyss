package com.mineinabyss.components.gondolas

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Represents a gondola ticket.
 * It controls the access to a list of gondolas in a route.
 **/

@Serializable
@SerialName("mineinabyss:gondola_ticket")
class Ticket (
    val gondolasInRoute: List<String>, // IDs of the gondolas in the route
    val ticketName: String, // Name of the ticket
    val ticketPrice: Int,
)
