package com.mineinabyss.helpers

import com.mineinabyss.components.playerData
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.mineinabyss.core.discordSRV
import com.mineinabyss.mineinabyss.core.isAbyssWorld
import com.mineinabyss.mineinabyss.core.layer
import com.ticxo.modelengine.api.ModelEngineAPI
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.*


data class ItemDrop(
    val material: Material,
    val dropAmount: IntRange,
    val applyFortune: Boolean = true
)

val luckPerms = LuckPermsProvider.get()

// Unicodes for whistles, also used for HappyHUD but through custom font file
fun Player.getLayerWhistleForHud() : String {
    val layer = PlainTextComponentSerializer.plainText().serialize(location.layer?.name?.miniMsg() ?: return "").lowercase()
    return when {
        "orth" in layer -> "\uEBAF"
        "edge" in layer -> "\uEBB0"
        "forest" in layer -> "\uEBB1"
        "great" in layer -> "\uEBB2"
        "goblets" in layer -> "\uEBB2"
        "sea" in layer -> "\uEBB3"
        else -> ""
    }
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

fun Player.getLinkedDiscordAccount(): String? {
    return runCatching { discordSRV.jda.getUserById(discordSRV.accountLinkManager.getDiscordId(uniqueId))?.name }.getOrNull() ?: null
}

fun Player.getGroups(): List<String> {
    return luckPerms.userManager.getUser(uniqueId)?.getNodes(NodeType.INHERITANCE)?.stream()
        ?.map { obj: InheritanceNode -> obj.groupName }?.toList() ?: emptyList()
}

fun Player.getFirstSimilarItem(item: ItemStack?) = inventory.contents?.firstOrNull { i -> i?.isSimilar(item) ?: false }
object MountUtils {
    /** Gets the entity the player is mounted on, be that vanilla or ModelEngine entity*/
    val Player.mount: LivingEntity?
        get() = (vehicle ?: ModelEngineAPI.getMountManager()?.getMountedPair(uniqueId)?.base?.original) as? LivingEntity
}

object CoinFactory {
    fun newOrthCoin() = LootyFactory.createFromPrefab(PrefabKey.Companion.of("mineinabyss", "orthcoin"))
    fun newMittyToken() = LootyFactory.createFromPrefab(PrefabKey.of("mineinabyss", "patreon_token"))
}

