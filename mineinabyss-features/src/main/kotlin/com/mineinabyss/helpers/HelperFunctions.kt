package com.mineinabyss.helpers

import com.ehhthan.happyhud.api.HudHolder
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.mineinabyss.core.discordSRV
import com.mineinabyss.mineinabyss.core.isAbyssWorld
import com.mineinabyss.mineinabyss.core.layer
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.atan2


data class ItemDrop(
    val material: Material,
    val dropAmount: IntRange,
    val applyFortune: Boolean = true
)

val luckPerms = LuckPermsProvider.get()

fun Player.toggleHud(toggle: Boolean? = null) {
    if (toggle == null) playerData.showPlayerBalance = !playerData.showPlayerBalance
    else playerData.showPlayerBalance = toggle

    if (playerData.showPlayerBalance && !HudHolder.has(this))
        HudHolder.holders().add(HudHolder.get(this))
    else if (!playerData.showPlayerBalance && HudHolder.has(this))
        HudHolder.holders().remove(HudHolder.get(this))
}

fun Player.bossbarCompass(loc: Location?, bar: BossBar) {
    if (loc == null || loc.world != world) {
        bar.name(Component.text(":arrow_null:"))
        return
    }

    val dir = loc.subtract(location).toVector()
    val angleDir = (atan2(dir.z, dir.x) / 2 / Math.PI * 360 + 180) % 360
    val angleLook = (atan2(location.direction.z, location.direction.x) / 2 / Math.PI * 360 + 180) % 360

    val barNameList = listOf(
        ":arrow_n:",
        ":arrow_nne:",
        ":arrow_ne:",
        ":arrow_ene:",
        ":arrow_e:",
        ":arrow_ese:",
        ":arrow_se:",
        ":arrow_sse:",
        ":arrow_s:",
        ":arrow_ssw:",
        ":arrow_sw:",
        ":arrow_wsw:",
        ":arrow_w:",
        ":arrow_wnw:",
        ":arrow_nw:",
        ":arrow_nnw:",
    )

    val compassAngle = (((angleDir - angleLook + 360) % 360) / 22.5).toInt()
    bar.name(Component.text(barNameList[compassAngle]))

    showBossBar(bar)
}

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

