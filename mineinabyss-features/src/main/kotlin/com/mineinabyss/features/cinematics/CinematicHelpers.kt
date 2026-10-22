package com.mineinabyss.features.cinematics

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.deeperworld.movement.transition.TransitionKind
import com.mineinabyss.deeperworld.player.canMoveSections
import com.mineinabyss.deeperworld.sections.correspondingLocation
import com.mineinabyss.deeperworld.sections.inSectionOverlap
import com.mineinabyss.deeperworld.sections.inSectionTransition
import com.mineinabyss.deeperworld.sections.withSection
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.hideHands
import com.mineinabyss.features.helpers.sendTransparentSlots
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.time.inWholeTicks
import kotlinx.coroutines.delay
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// A new entity reaches the client on the tracker tick after it spawns, a camera packet or mount sent before that is ignored
internal const val TRACKER_TICKS = 3
internal const val TICKS_PER_SECOND = 20.0
// A step up or down, further than this the path's own height is kept, e.g. stepping off an edge onto a model
private const val GROUND_SEARCH = 0.6
private val HIDDEN_KEY = NamespacedKey("mineinabyss", "cinematic/hidden")
private val BODY_REHIDE = 1.seconds
// Armor slots of the player's own inventory menu, head to feet
private val ARMOR_SLOTS = 5..8
private val BODY_INVISIBILITY = PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, false)

internal fun Duration.tickCount() = inWholeTicks.toInt().coerceAtLeast(1)

internal fun ground(location: Location, search: Double = GROUND_SEARCH): Location {
    val start = location.clone().add(0.0, search, 0.0)
    val hit = location.world.rayTraceBlocks(start, Vector(0, -1, 0), search * 2, FluidCollisionMode.NEVER, true)
    return hit?.let { location.clone().apply { y = it.hitPosition.y } } ?: location
}

/**
 * Riders are always drawn sitting, which the player sees of themselves in F5 even at zero distance.
 * Invisibility hides the body but not armor or held items, so those get transparent items on the player's own client, sent again in case an inventory update brings them back
 */
internal fun CinematicSession.hideBody() {
    if (player.persistentDataContainer.has(HIDDEN_KEY)) return
    player.persistentDataContainer.set(HIDDEN_KEY, PersistentDataType.BOOLEAN, true)
    abyss.launch {
        while (!restored && player.isOnline) {
            // An invisibility potion the player already has is left to run out, then replaced by ours
            if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) player.addPotionEffect(BODY_INVISIBILITY)
            player.hideHands()
            player.sendTransparentSlots(ARMOR_SLOTS)
            delay(BODY_REHIDE)
        }
    }
}

internal fun showBody(player: Player) {
    if (!player.persistentDataContainer.has(HIDDEN_KEY)) return
    player.persistentDataContainer.remove(HIDDEN_KEY)
    if (player.getPotionEffect(PotionEffectType.INVISIBILITY) == BODY_INVISIBILITY) player.removePotionEffect(PotionEffectType.INVISIBILITY)
}

/** DeeperWorld lookups that do nothing when it is not installed */
internal object Sections {
    private val enabled get() = Plugins.isEnabled("DeeperWorld")

    /** Where [location] continues in the section below, once it is far enough into the overlap that DeeperWorld would move a player */
    fun below(location: Location): Location? = if (enabled && location.inSectionTransition) descending(location) else null

    /** Where [location] continues in the section below, from as soon as it enters the overlap */
    fun upcoming(location: Location): Location? = if (enabled && location.inSectionOverlap) descending(location) else null

    private fun descending(location: Location): Location? {
        if (location.withSection?.kind != TransitionKind.DESCEND) return null
        return location.correspondingLocation
    }

    // DeeperWorld would otherwise move the hidden player on its own when they pass a section edge
    fun setCanMove(player: Player, canMove: Boolean) {
        if (enabled) player.canMoveSections = canMove
    }
}
