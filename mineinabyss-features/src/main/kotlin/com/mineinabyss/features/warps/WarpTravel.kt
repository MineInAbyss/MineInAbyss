package com.mineinabyss.features.warps

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.actions.OpenDialogAction
import com.mineinabyss.features.dialogs.conditions.DialogCondition
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.dialogs.loading.DialogVariables
import com.mineinabyss.features.dialogs.model.ActionNode
import com.mineinabyss.features.dialogs.model.Answer
import com.mineinabyss.features.dialogs.model.ConditionalNode
import com.mineinabyss.features.dialogs.model.DialogRef
import com.mineinabyss.features.dialogs.model.LockedAnswer
import com.mineinabyss.features.dialogs.model.PageNode
import com.mineinabyss.features.dialogs.model.ReplyNode
import com.mineinabyss.features.dialogs.runtime.SPEAKER_KEY
import com.mineinabyss.features.dialogs.runtime.STYLE_KEY
import com.mineinabyss.features.npc.NPC_ENTITY_KEY
import com.mineinabyss.features.npc.NPC_KEY
import com.mineinabyss.features.npc.NpcDefinition
import com.mineinabyss.features.npc.npcSpawner
import com.mineinabyss.geary.actions.Task
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.error
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

private const val WARP_VARIABLE = "warp"

/** A resolved trip, [route] is null when the player may not travel it */
class Trip(val key: PrefabKey, val warp: WarpDefinition, val from: String, val to: String, val route: WarpRoute?) {
    val cost get() = route?.let { warp.costOf(it, from, to) }
}

class WarpTravel(
    private val config: WarpsConfig,
    private val registry: WarpRegistry,
    private val warps: Warps,
) {
    /**
     * [from] defaults to the stop nearest the player.
     * [to] defaults to the only stop the player can reach from there
     */
    fun trip(player: Player, key: PrefabKey, to: String? = null, from: String? = null, notify: Boolean = true): Trip? {
        val warp = registry[key] ?: return null.also { abyss.logger.w("Unknown warp $key") }
        val origin = from ?: warp.nearestStop(player.location) ?: return null.also { if (notify) player.error("You are not near ${warp.name}") }
        val destination = to ?: warp.stops.keys.singleOrNull { it != origin } ?: run {
            val reachable = warp.destinationsFrom(player, origin)
            reachable.singleOrNull() ?: return null.also {
                if (reachable.isNotEmpty()) abyss.logger.w("Warp $key has several destinations from $origin, a destination must be given")
                else if (notify) player.error("You cannot travel anywhere from ${warp.stopName(origin)}")
            }
        }

        return Trip(key, warp, origin, destination, warp.routeFor(player, origin, destination))
    }

    fun canTravel(player: Player, trip: Trip, checkCost: Boolean = true): Boolean {
        return !warps.isWarping(player) && (!checkCost || (trip.cost ?: return false).canAfford(player))
    }

    fun travel(player: Player, key: PrefabKey, to: String? = null, from: String? = null) {
        val trip = trip(player, key, to, from) ?: return
        val cost = trip.cost ?: return player.error("You cannot travel to ${trip.warp.stopName(trip.to)} from here")
        if (warps.isWarping(player)) return
        if (!cost.canAfford(player)) return player.error("You need ${cost.describe()} to travel to ${trip.warp.stopName(trip.to)}")

        val destination = trip.warp.stops.getValue(trip.to).destination
        // Players riding a warp with trigger blocks are placed inside it before leaving, like boarding a gondola
        val departure = trip.warp.stops.getValue(trip.from).center.takeIf { trip.warp.triggers }
        warps.warp(player, destination, config.transition(trip.warp.transition), departure, cost)
    }

    /** An NPC in [environment], like the one that ran `warp_offer`, speaks the dialog */
    fun offer(player: Player, key: PrefabKey, to: String? = null, from: String? = null, environment: Map<String, Any?> = emptyMap()) {
        // Opened even when the route is closed, so the dialog can tell the player how to open it
        val trip = trip(player, key, to, from) ?: return
        val sessions = dialogSessions ?: return abyss.logger.w("Dialogs feature is not loaded, cannot offer warp $key")
        val base = trip.warp.dialog ?: config.defaultDialog
        val variables = dialogVariables(trip).mapValues { YamlScalar(it.value, YamlPath.root) }
        val node = runCatching { sessions.registry.copyOf(base, variables) }
            .getOrElse { return abyss.logger.e("Could not build warp dialog $base for $key: ${it.message}") }

        val npc = callingNpc(environment) ?: npcAt(trip)
        val speaker = npc?.definition?.speakerName?.takeIf { it.isNotBlank() } ?: trip.warp.name
        val focus = npc?.entity?.takeIf { trip.warp.forcePlayerFacingNPC && it.isValid }

        sessions.start(player, node, "warp_${key.full}", speaker, npc?.definition?.dialogStyle, environment, focus)
    }

    fun stopNear(player: Player, key: PrefabKey): String? = registry[key]?.nearestStop(player.location)

    /**
     * Lists every stop in order with the price of getting there shown while it is selected.
     * The stop the player is at is greyed out and starts selected, so the list reads as directions along the line
     */
    fun menu(player: Player, key: PrefabKey, menu: WarpMenuAction, environment: Map<String, Any?>) {
        val warp = registry[key] ?: return abyss.logger.w("Unknown warp $key")
        val here = warp.nearestStop(player.location) ?: return player.error("You are not near ${warp.name}")
        val sessions = dialogSessions ?: return abyss.logger.w("Dialogs feature is not loaded, cannot open warp menu $key")

        val node = ReplyNode(
            lines = menu.lines.map { it.replace("<from_name>", warp.stopName(here)) },
            answers = warp.stops.keys.map { stop -> stopAnswer(player, key, warp, here, stop, menu) } +
                listOfNotNull(menu.leave.takeIf { it.isNotBlank() }?.let { Answer(it) }),
        )
        sessions.start(player, node, "warp_menu_${key.full}", environment[SPEAKER_KEY] as? String, environment[STYLE_KEY] as? String, environment)
    }

    private fun stopAnswer(player: Player, key: PrefabKey, warp: WarpDefinition, here: String, stop: String, menu: WarpMenuAction): Answer {
        val canWarp = DialogCondition(CanWarpCondition(key, stop))
        if (stop == here) return Answer(
            text = warp.stopName(stop),
            conditions = listOf(canWarp),
            lockedText = menu.here.replace("<stop_name>", warp.stopName(stop)),
            selectedWhen = listOf(DialogCondition(AtStopCondition(key, stop))),
        )

        val price = trip(player, key, stop, here, notify = false)?.cost?.describe() ?: ""
        return Answer(
            text = warp.stopName(stop),
            info = menu.info.replace("<price>", price),
            conditions = listOf(DialogCondition(CanWarpCondition(key, stop, checkCost = false))),
            whenLocked = LockedAnswer.HIDE,
            next = DialogRef.Inline(
                ConditionalNode(
                    conditions = listOf(canWarp),
                    onTrue = DialogRef.Inline(ActionNode(actions = Tasks(listOf(Task(WarpTravelAction(key, stop)))))),
                    onFalse = DialogRef.Inline(PageNode(lines = menu.cannotPay.map { it.replace("<price>", price) })),
                )
            ),
        )
    }

    private fun dialogVariables(trip: Trip): Map<String, String> = mapOf(
        WARP_VARIABLE to trip.key.full,
        "warp_name" to trip.warp.name,
        "from" to trip.from,
        "from_name" to trip.warp.stopName(trip.from),
        "destination" to trip.to,
        "destination_name" to trip.warp.stopName(trip.to),
        "price" to (trip.cost?.describe() ?: ""),
    )

    private class StopNpc(val definition: NpcDefinition, val entity: Entity?)

    private fun callingNpc(environment: Map<String, Any?>): StopNpc? {
        val definition = environment[NPC_KEY] as? NpcDefinition ?: return null
        return StopNpc(definition, environment[NPC_ENTITY_KEY] as? Entity)
    }

    /** The stop's own [WarpStop.npc], otherwise the nearest NPC that offers this warp */
    private fun npcAt(trip: Trip): StopNpc? {
        val spawner = npcSpawner ?: return null
        val stop = trip.warp.stops.getValue(trip.from)
        stop.npc?.let { key ->
            val npc = spawner.npcs[key] ?: return null.also { abyss.logger.w("Warp ${trip.key} stop ${trip.from} names unknown npc $key") }
            return StopNpc(npc, spawner.entity(key))
        }

        val (key, npc) = spawner.nearest(stop.location) { it.servesWarp(trip.key) } ?: return null
        return StopNpc(npc, spawner.entity(key))
    }

    private fun NpcDefinition.servesWarp(key: PrefabKey) = onInteract?.tasks.orEmpty().any { task ->
        when (val action = task.action) {
            is WarpOfferAction -> action.warp == key
            is WarpTravelAction -> action.warp == key
            is WarpMenuAction -> action.warp == key
            is OpenDialogAction -> (action.dialog as? YamlMap)?.get<YamlMap>(DialogVariables.VARIABLES)?.getScalar(WARP_VARIABLE)?.content == key.full
            else -> false
        }
    }
}
