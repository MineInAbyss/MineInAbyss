package com.mineinabyss.features.cinematics

import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.runtime.SESSION_KEY
import com.mineinabyss.features.helpers.player
import com.mineinabyss.features.npc.NPC_KEY
import com.mineinabyss.features.npc.NpcDefinition
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

enum class AnchorSource {
    /** The configured location of the NPC that was talked to, falls back to [PLAYER] */
    NPC,
    /** Where the player stands, facing the way they face */
    PLAYER,
    /** The action's `location` */
    LOCATION,
}

@Serializable
@SerialName("mineinabyss:cinematic")
class CinematicAction(
    val scene: PrefabKey,
    val anchor: AnchorSource = AnchorSource.NPC,
    val location: @Serializable(LocationAltSerializer::class) Location? = null,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val cinematics = cinematics ?: return abyss.logger.w("Cinematics feature is not loaded, cannot play $scene")
        val origin = when (anchor) {
            AnchorSource.NPC -> (environment[NPC_KEY] as? NpcDefinition)?.location
            AnchorSource.LOCATION -> location ?: return abyss.logger.w("Cinematic $scene is anchored to a location but none is given")
            AnchorSource.PLAYER -> null
        } ?: player.location
        // Playing closes the dialog this was run from
        cinematics.play(player, scene, CinematicAnchor(origin), environment.toMap() - SESSION_KEY)
    }
}
