package com.mineinabyss.features.helpers

import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.discordSRV
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
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

val Player.simpleLayerName: String
    get() = PlainTextComponentSerializer.plainText().serialize(location.layer?.name?.miniMsg() ?: Component.empty()).lowercase().replace(" ", "_")

// Unicodes for whistles, also used for HappyHUD but through custom font file
fun Player.getLayerWhistleForHud(): String {
    return when(simpleLayerName) {
        "orth" -> "\uEBAF"
        "edge_of_the_abyss" -> "\uEBB0"
        "forest_of_temptation" -> "\uEBB1"
        "great_fault" -> "\uEBB2"
        "the_goblets_of_giants" -> "\uEBB2"
        "sea_of_corpses" -> "\uEBB3"
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

val Player.linkedDiscordAccount
    get() = runCatching {
        discordSRV.jda.getUserById(discordSRV.accountLinkManager.getDiscordId(uniqueId))?.name
    }.getOrNull()

val Player.luckpermGroups
    get() = luckPerms.userManager.getUser(uniqueId)?.getNodes(NodeType.INHERITANCE)?.stream()
        ?.map { obj: InheritanceNode -> obj.groupName }?.toList() ?: emptyList()

/*object MountUtils {
    *//** Gets the entity the player is mounted on, be that vanilla or ModelEngine entity*//*
    val Player.mount: LivingEntity?
        get() = (vehicle ?: ModelEngineAPI.getMountManager()?.getMountedPair(uniqueId)?.base?.original) as? LivingEntity
}*/

object CoinFactory {
    val orthCoin get() = gearyItems.createItem(PrefabKey.of("mineinabyss", "orthcoin"))
    val mittyToken get() = gearyItems.createItem(PrefabKey.of("mineinabyss", "patreon_token"))
}

val Player.isInventoryFull: Boolean
    get() = inventory.firstEmpty() == -1
