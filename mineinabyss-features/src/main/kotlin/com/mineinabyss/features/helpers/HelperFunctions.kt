package com.mineinabyss.features.helpers

import com.mineinabyss.components.curse.PlayerCurseEvent
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.features.helpers.api.API
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap


data class ItemDrop(
    val material: Material,
    val dropAmount: IntRange,
    val applyFortune: Boolean = true
)

val Player.simpleLayerName: String
    get() = PlainTextComponentSerializer.plainText().serialize(location.layer?.name?.miniMsg() ?: Component.empty()).lowercase().replace(" ", "_")

// Unicodes for whistles, also used for HappyHUD but through custom font file
fun Player.getLayerWhistleForHud(): String {
    return when(simpleLayerName) {
        "orth" -> "\uEBAF"
        "edge_of_the_abyss" -> "\uEBB0"
        "forest_of_temptation" -> "\uEBB1"
        "great_fault", "the_goblets_of_giants" -> "\uEBB2"
        "sea_of_corpses" -> "\uEBB3"
        else -> ""
    }
}

/** Blocks of ascent that need to accrue before a curse triggers */
const val CURSE_THRESHOLD = 10.0

/** Failsafe so a single absurd movement cant spam effects, normal movement never comes close */
private const val MAX_CURSE_APPLICATIONS_PER_MOVE = 5

/** How long after a section change a movement is still considered part of that teleport */
private const val SECTION_CHANGE_GRACE_MS = 1000L
private val recentSectionChanges = ConcurrentHashMap<UUID, Long>()

/** Marks a player as having just changed section, so the movement caused by DeeperWorld's teleport is not counted as ascent */
fun markRecentSectionChange(player: Player) {
    recentSectionChanges[player.uniqueId] = System.currentTimeMillis()
}

fun forgetRecentSectionChange(player: Player) {
    recentSectionChanges.remove(player.uniqueId)
}

private fun Player.consumeRecentSectionChange(): Boolean {
    val changedAt = recentSectionChanges.remove(uniqueId) ?: return false
    return System.currentTimeMillis() - changedAt <= SECTION_CHANGE_GRACE_MS
}

fun handleCurse(player: Player, from: Location, to: Location) {
    if (from.world != to.world) return
    if (!player.world.isAbyssWorld) return
    if (player.consumeRecentSectionChange()) return

    player.editPlayerData {
        if (player.isInvulnerable || !isAffectedByCurse) {
            curseAccrued = 0.0
            return@editPlayerData
        } else curseAccrued = (curseAccrued + to.y - from.y).coerceAtLeast(0.0)
        val ascensionEffects = to.layer?.ascensionEffects ?: return@editPlayerData

        var applications = 0
        while (curseAccrued >= CURSE_THRESHOLD && applications++ < MAX_CURSE_APPLICATIONS_PER_MOVE) {
            if (!PlayerCurseEvent(player, ascensionEffects).callEvent()) break
            for (effect in ascensionEffects) effect.applyEffect(player, CURSE_THRESHOLD.toInt())
            curseAccrued -= CURSE_THRESHOLD
        }
    }
}

val OfflinePlayer.linkedDiscordAccount
    get() = API.DiscordSRV?.plugin?.runCatching { jda.getUserById(accountLinkManager.getDiscordId(uniqueId))?.name }?.getOrNull()
