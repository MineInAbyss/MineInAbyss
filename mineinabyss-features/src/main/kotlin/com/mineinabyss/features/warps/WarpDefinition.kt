package com.mineinabyss.features.warps

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.features.dialogs.conditions.DialogCondition
import com.mineinabyss.features.dialogs.conditions.allPass
import com.mineinabyss.features.dialogs.model.DialogConditionsSerializer
import com.mineinabyss.features.dialogs.runtime.dialogContext
import com.mineinabyss.features.helpers.Cost
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Location
import org.bukkit.entity.Player

@Serializable
class WarpDefinition(
    val name: String,
    val stops: Map<String, @Serializable(WarpStopShorthand::class) WarpStop>,
    /** Empty lets every stop reach every other stop, otherwise only the listed routes can be travelled */
    val routes: List<WarpRoute> = emptyList(),
    /** Used by stops and routes without a cost of their own */
    val cost: Cost? = null,
    val transition: TransitionRef? = null,
    val dialog: PrefabKey? = null,
    /** Places trigger blocks on every stop that offer this warp when stepped on */
    val triggers: Boolean = false,
    val forcePlayerFacingNPC: Boolean = false,
) {
    /** The first listed route between [from] and [to] whose conditions pass, null if the player cannot travel it */
    fun routeFor(player: Player, from: String, to: String): WarpRoute? {
        if (from == to || from !in stops || to !in stops) return null
        if (routes.isEmpty()) return WarpRoute()
        val context = dialogContext(player)

        return routes.firstOrNull { it.connects(from, to) && it.conditions.allPass(context) }
    }

    /** A route's own cost wins, otherwise stops are a line in the order they are listed and a trip pays for every stop it enters on the way */
    fun costOf(route: WarpRoute, from: String, to: String): Cost {
        route.cost?.let { return it }
        val order = stops.keys.toList()
        val start = order.indexOf(from)
        val end = order.indexOf(to)
        val forward = end > start
        val entered = if (forward) order.subList(start + 1, end + 1) else order.subList(end, start)

        return entered.map { stopCost(it, forward) }.fold(Cost.FREE, Cost::plus)
    }

    private fun stopCost(id: String, forward: Boolean): Cost {
        val stop = stops.getValue(id)
        return (if (forward) stop.costForward else stop.costBackward) ?: stop.cost ?: cost ?: Cost.FREE
    }

    fun destinationsFrom(player: Player, from: String): List<String> = stops.keys.filter { routeFor(player, from, it) != null }

    fun nearestStop(location: Location): String? = stops.entries
        .filter { it.value.location.world == location.world }
        .minByOrNull { it.value.location.distanceSquared(location) }
        ?.key

    fun stopName(id: String) = stops[id]?.name ?: id
}

/** `from` or `to` left out matches any stop */
@Serializable
class WarpRoute(
    val from: String? = null,
    val to: String? = null,
    val conditions: @Serializable(DialogConditionsSerializer::class) List<DialogCondition> = emptyList(),
    val cost: Cost? = null,
) {
    fun connects(from: String, to: String) = (this.from == null || this.from == from) && (this.to == null || this.to == to)
}

/** Written as just a location, or as a map with a `name`, trigger block `offsets`, a landing `offset` area, costs and an `npc` */
@Serializable
@ExcludeAutoScan
class WarpStop(
    val location: @Serializable(LocationAltSerializer::class) Location,
    val name: String? = null,
    val offsets: @Serializable(OffsetAreasSerializer::class) List<OffsetArea> = emptyList(),
    val offset: @Serializable(OffsetAreaSerializer::class) OffsetArea = OffsetArea.NONE,
    val cost: Cost? = null,
    /** Paid instead of [cost] when arriving from a stop listed before this one */
    val costForward: Cost? = null,
    /** Paid instead of [cost] when arriving from a stop listed after this one */
    val costBackward: Cost? = null,
    /** Speaks this stop's ride dialog, left out it is the nearest NPC that offers the warp */
    val npc: PrefabKey? = null,
) {
    val center: Location get() = location.toBlockLocation().add(0.5, 0.0, 0.5).apply { yaw = location.yaw; pitch = location.pitch }

    val destination get() = WarpDestination(center, offset)

    fun triggerBlocks(): List<Location> {
        val origin = location.toBlockLocation()
        val around = offsets.flatMap { area -> area.blockOffsets().map { (x, y, z) -> origin.clone().add(x.toDouble(), y.toDouble(), z.toDouble()) } }
        return (listOf(origin) + around).distinct()
    }
}

object WarpStopShorthand : KSerializer<WarpStop> {
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

    override fun deserialize(decoder: Decoder): WarpStop {
        if ((decoder as? YamlInput)?.node is YamlScalar) return WarpStop(LocationAltSerializer.deserialize(decoder))
        return WarpStop.serializer().deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: WarpStop) = WarpStop.serializer().serialize(encoder, value)
}
