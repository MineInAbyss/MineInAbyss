package com.mineinabyss.features.cinematics

import com.mineinabyss.deeperworld.movement.transition.TransitionKind
import com.mineinabyss.deeperworld.player.canMoveSections
import com.mineinabyss.deeperworld.sections.correspondingLocation
import com.mineinabyss.deeperworld.sections.inSectionOverlap
import com.mineinabyss.deeperworld.sections.inSectionTransition
import com.mineinabyss.deeperworld.sections.withSection
import com.mineinabyss.idofront.plugin.Plugins
import org.bukkit.Location
import org.bukkit.entity.Player

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
