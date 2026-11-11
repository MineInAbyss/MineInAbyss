package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.config.WardrobeSettings
import com.hibiscusmc.hmccosmetics.config.section.Wardrobe
import com.hibiscusmc.hmccosmetics.config.section.WardrobeLocation
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.helpers.ScalarShorthandSerializer
import com.mineinabyss.features.helpers.cosmeticUser
import com.mineinabyss.features.helpers.isInCinematicOrTransition
import com.mineinabyss.features.helpers.player
import com.mineinabyss.features.locations.LocationPoint
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

/**
 * Written as just a wardrobe id, or as a map of its fields
 * @param wardrobe id of a wardrobe in HMCCosmetics' config.yml, its permission still applies
 * @param npc where the player's mannequin stands
 * @param viewer where the camera looks at the mannequin from
 * @param leave left out returns the player to where they stood, HMCCosmetics' return-last-location overrides it
 * @param menu an HMCCosmetics menu id, left out uses its default menu
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(OpenWardrobeAction.Serializer::class)
class OpenWardrobeAction(
    val wardrobe: String? = null,
    val npc: LocationPoint? = null,
    val viewer: LocationPoint? = null,
    val leave: LocationPoint? = null,
    val menu: String? = null,
) : Action {

    init {
        require(wardrobe != null || (npc != null && viewer != null)) { "open_wardrobe needs either a wardrobe id or both npc and viewer" }
        require(wardrobe == null || (npc == null && viewer == null && leave == null && menu == null)) { "open_wardrobe takes either a wardrobe id or its own locations, not both" }
    }

    private val id get() = wardrobe ?: "$npc"

    private fun resolve(player: Player): Wardrobe? {
        wardrobe?.let { return WardrobeSettings.getWardrobe(it) }
        val location = WardrobeLocation(npc!!.resolve(), viewer!!.resolve(), leave?.resolve() ?: player.location)
        return Wardrobe("mineinabyss_$npc", location, null, -1, menu, null)
    }

    override fun ActionGroupContext.execute() {
        val player = player ?: return
        if (!abyss.isHMCCosmeticsEnabled) return abyss.logger.w("HMCCosmetics is not enabled, cannot open wardrobe $id")
        if (player.isInCinematicOrTransition())
            return abyss.logger.w("open_wardrobe $id refused for ${player.name}, they are in a transition or cinematic")
        val user = player.cosmeticUser?.takeUnless { it.isInWardrobe } ?: return
        val target = resolve(player) ?: return abyss.logger.w("Unknown HMCCosmetics wardrobe $id in open_wardrobe")

        dialogSessions?.close(player)
        user.enterWardrobe(target, true)
    }

    object Serializer : ScalarShorthandSerializer<OpenWardrobeAction>(
        generatedSerializer(), { OpenWardrobeAction(wardrobe = it.decodeString()) }, "mineinabyss:open_wardrobe"
    )
}
