package com.mineinabyss.features.warps

import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.runtime.player
import com.mineinabyss.features.helpers.Cost
import com.mineinabyss.features.helpers.loaded
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.error
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private inline fun <reified T : Any> warpFeature(warn: Boolean = true): T? =
    loaded<T>(WarpFeature) ?: null.also { if (warn) abyss.logger.w("Warps feature is not loaded") }

/** Travels straight to a location, without a warp file */
@Serializable
@SerialName("mineinabyss:warp")
class WarpAction(
    val to: @Serializable(WarpDestinationShorthand::class) WarpDestination,
    val transition: TransitionRef? = null,
    val cost: Cost = Cost.FREE,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val warps = warpFeature<Warps>() ?: return
        if (warps.isWarping(player)) return
        if (!cost.canAfford(player)) return player.error("You need ${cost.describe()} for this")
        warps.warp(player, to, warps.config.transition(transition), cost = cost)
    }
}

/** [to] and [from] are stop names, see [WarpTravel.trip] for their defaults */
@Serializable
@SerialName("mineinabyss:warp_travel")
class WarpTravelAction(val warp: PrefabKey, val to: String? = null, val from: String? = null) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        warpFeature<WarpTravel>()?.travel(player, warp, to, from)
    }
}

/** Opens the warp's ride dialog, the same one its trigger blocks open */
@Serializable
@SerialName("mineinabyss:warp_offer")
class WarpOfferAction(val warp: PrefabKey, val to: String? = null, val from: String? = null) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        warpFeature<WarpTravel>()?.offer(player, warp, to, from, environment.toMap())
    }
}

/** Passes when a route's conditions pass and, unless [checkCost] is false, the player can pay for it */
@Serializable
@SerialName("mineinabyss:can_warp")
class CanWarpCondition(
    val warp: PrefabKey,
    val to: String? = null,
    val from: String? = null,
    val checkCost: Boolean = true,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val player = player ?: return false
        val travel = warpFeature<WarpTravel>(warn = false) ?: return false
        val trip = travel.trip(player, warp, to, from, notify = false) ?: return false
        return travel.canTravel(player, trip, checkCost)
    }
}

/** Passes when [stop] is the stop of [warp] nearest the player */
@Serializable
@SerialName("mineinabyss:at_stop")
class AtStopCondition(val warp: PrefabKey, val stop: String) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val player = player ?: return false
        return warpFeature<WarpTravel>(warn = false)?.stopNear(player, warp) == stop
    }
}

/**
 * Opens a menu of every stop in [warp], see [WarpTravel.menu].
 * `<from_name>` in [lines] is the stop the player is at, `<stop_name>` in [here] is that stop, `<price>` is the selected trip's price.
 * A blank [leave] leaves out the answer that closes the menu
 */
@Serializable
@SerialName("mineinabyss:warp_menu")
class WarpMenuAction(
    val warp: PrefabKey,
    val lines: List<String> = listOf("Where do you want to go?"),
    val here: String = "<#9c9586><stop_name>",
    val info: String = "Price: <price>",
    val cannotPay: List<String> = listOf("You cannot pay for that trip,", "it costs <price>."),
    val leave: String = "Not now",
) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        warpFeature<WarpTravel>()?.menu(player, warp, this@WarpMenuAction, environment.toMap())
    }
}
