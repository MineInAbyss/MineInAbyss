package com.mineinabyss.helpers

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.mineinabyss.core.*
import com.mineinabyss.playerprofile.luckPerms
import kotlinx.coroutines.delay
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.atan2
import kotlin.time.Duration.Companion.seconds


fun dropItems(loc: Location, drop: ItemStack) {
    loc.world.dropItem(loc.add(Vector.getRandom().subtract(Vector(.5, .5, .5)).multiply(0.5)), drop).velocity =
        Vector.getRandom().add(Vector(-.5, +.5, -.5)).normalize().multiply(.15)
}

data class ItemDrop(
    val material: Material,
    val dropAmount: IntRange,
    val applyFortune: Boolean = true
)

fun Player.isInHub() = MIAConfig.data.hubSection == player?.location?.let { WorldManager.getSectionFor(it) }

fun Player.updateBalance() {
    val data = player?.playerData
    val orthCoinBalance = data?.orthCoinsHeld
    val mittyTokenBalance = data?.mittyTokensHeld
    val splitBalance = orthCoinBalance.toString().toList().joinToString { ":banking_$it:" }.replace(", ", "")
    val splitSupporterBalance = mittyTokenBalance.toString().toList().joinToString { ":banking_$it:" }.replace(", ", "")

    val currentBalance: Component = if (data?.mittyTokensHeld!! > 0) {
        Component.text("${Space.of(128)}${splitBalance}:orthcoin: $splitSupporterBalance:mittytoken:")
    } else Component.text("${Space.of(160)}${splitBalance}:orthcoin:")

    if (data.orthCoinsHeld < 0) data.orthCoinsHeld = 0

    mineInAbyss.launch {
        do {
            player?.sendActionBar(currentBalance)
            delay(1.seconds)
        } while ((data.orthCoinsHeld == orthCoinBalance) && (data.mittyTokensHeld == mittyTokenBalance) && data.showPlayerBalance)
        return@launch
    }
}

fun Player.bossbarCompass(loc: Location, bar: BossBar) {
    if (loc.world != player?.world) {
        bar.name(Component.text(":arrow_null:"))
        return
    }
    val player = player ?: return
    val dir = loc.subtract(player.location).toVector()
    val angleDir = (atan2(dir.z, dir.x) / 2 / Math.PI * 360 + 180) % 360
    val angleLook = (atan2(player.location.direction.z, player.location.direction.x) / 2 / Math.PI * 360 + 180) % 360

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

    player.hideBossBar(bar)
    player.showBossBar(bar)
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
    val playerData = player.playerData

    playerData.apply {
        if (player.isInvulnerable) {
            curseAccrued = 0.0
        } else if (playerData.isAffectedByCurse) {
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

fun Player.getLinkedDiscordAccount() : String? {
    return runCatching { discordSRV.jda.getUserById(discordSRV.accountLinkManager.getDiscordId(player?.uniqueId))?.name }.getOrNull()
}

fun Player.getGroups() : List<String> {
    return luckPerms.userManager.getUser(player?.uniqueId!!)?.getNodes(NodeType.INHERITANCE)?.stream()
        ?.map { obj: InheritanceNode -> obj.groupName }?.toList() ?: emptyList()
}

