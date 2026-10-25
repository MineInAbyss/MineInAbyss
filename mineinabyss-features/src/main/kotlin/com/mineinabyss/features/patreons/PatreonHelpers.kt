package com.mineinabyss.features.patreons

import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.components.players.PatreonPerks
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import net.luckperms.api.node.NodeType
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

const val KEEP_NICKNAME_PERMISSION = "mineinabyss.patreon.keepnickname"

/** Matches prefixes written by the old per-layer `/patreon prefix set`, capturing the emote */
private val legacyPrefix = Regex("""^:layer_\w+::space_2::(.+?)::space_2:<#\p{XDigit}{6}>$""")
private val legacyPrefixPriorities = 10..15

val OfflinePlayer.patreonTier: Int
    get() {
        val user = luckPerms.userManager.getUser(uniqueId)
            ?: runCatching { luckPerms.userManager.loadUser(uniqueId).get(4, TimeUnit.SECONDS) }.getOrNull()
            ?: return 0
        val tiers = abyss.config.patreon.groupTiers
        return user.getInheritedGroups(user.queryOptions).maxOfOrNull { tiers[it.name] ?: 0 } ?: 0
    }

val Player.patreonPerks get() = toGeary().get<PatreonPerks>() ?: PatreonPerks()

fun Player.editPatreonPerks(edit: PatreonPerks.() -> PatreonPerks) = toGeary().setPersisting(patreonPerks.edit())

val Player.patreonEmotePrefix: String
    get() {
        val emote = patreonPerks.emote ?: return ""
        if (patreonTier == 0) return ""
        // The LuckPerms prefix leaves its layer colour open for the nickname, closing white restores it after the emote
        return "<white>:$emote::space_2:</white>"
    }

fun Player.revokeLapsedPatreonPerks() {
    if (chattyNickname == null || patreonTier > 0 || hasPermission(KEEP_NICKNAME_PERMISSION)) return
    chattyNickname = null
    abyss.logger.i("Removed nickname from $name as they are no longer a Patreon supporter")
}

fun Player.migrateLegacyPatreonPrefix() {
    val user = luckPerms.userManager.getUser(uniqueId) ?: return
    val legacy = user.getNodes(NodeType.PREFIX).filter {
        it.priority in legacyPrefixPriorities && legacyPrefix.matches(it.metaValue)
    }.takeUnless { it.isEmpty() } ?: return

    val emote = legacyPrefix.find(legacy.minBy { it.priority }.metaValue)?.groupValues?.get(1)
    if (emote != null && patreonPerks.emote == null) editPatreonPerks { copy(emote = emote) }
    luckPerms.userManager.modifyUser(uniqueId) { modified -> legacy.forEach { modified.data().remove(it) } }
    abyss.logger.i("Migrated legacy Patreon prefix of $name to emote $emote")
}
