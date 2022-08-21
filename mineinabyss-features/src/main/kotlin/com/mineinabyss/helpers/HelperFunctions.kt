package com.mineinabyss.helpers

import com.mineinabyss.components.playerData
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.mineinabyss.core.discordSRV
import com.mineinabyss.mineinabyss.core.isAbyssWorld
import com.mineinabyss.mineinabyss.core.layer
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*


data class ItemDrop(
    val material: Material,
    val dropAmount: IntRange,
    val applyFortune: Boolean = true
)

val luckPerms = LuckPermsProvider.get()

private val recentlyMovedPlayers: MutableSet<UUID> = HashSet()
fun handleCurse(player: Player, from: Location, to: Location) {
    //Arbitrary range with the purpose of preventing curse on section change
    if (from.distanceSquared(to) > 32 * 32) return

    if (recentlyMovedPlayers.contains(player.uniqueId)) {
        recentlyMovedPlayers.remove(player.uniqueId)
        return
    }

    if (!player.world.isAbyssWorld) return

    val changeY = to.y - from.y

    player.playerData.apply {
        if (player.isInvulnerable) {
            curseAccrued = 0.0
        } else if (player.playerData.isAffectedByCurse) {
            val layer = to.layer ?: return

            val dist = curseAccrued
            curseAccrued = (dist + changeY).coerceAtLeast(0.0)
            if (dist >= 10) {
                layer.ascensionEffects.forEach {
                    it.clone().applyEffect(player, 10)
                }
                curseAccrued -= 10
            }
        }
    }
}

fun createOrthCoin() = LootyFactory.createFromPrefab(PrefabKey.Companion.of("mineinabyss", "orthcoin"))
fun createMittyToken() = LootyFactory.createFromPrefab(PrefabKey.of("mineinabyss", "patreon_token"))

fun Player.getLinkedDiscordAccount(): String? {
    return runCatching { discordSRV.jda.getUserById(discordSRV.accountLinkManager.getDiscordId(player?.uniqueId))?.name }.getOrNull()
}

fun Player.getGroups(): List<String> {
    return luckPerms.userManager.getUser(player?.uniqueId!!)?.getNodes(NodeType.INHERITANCE)?.stream()
        ?.map { obj: InheritanceNode -> obj.groupName }?.toList() ?: emptyList()
}

