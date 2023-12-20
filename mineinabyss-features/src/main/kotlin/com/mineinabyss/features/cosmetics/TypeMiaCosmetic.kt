package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.gui.type.types.TypeCosmetic
import com.hibiscusmc.hmccosmetics.user.CosmeticUser
import com.mineinabyss.components.playerData
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import me.lojosho.shaded.configurate.ConfigurationNode
import net.luckperms.api.node.Node
import org.bukkit.event.inventory.ClickType


class TypeMiaCosmetic : TypeCosmetic("mia_cosmetic") {

    override fun run(user: CosmeticUser?, config: ConfigurationNode, clickType: ClickType?) {
        val player = user?.player ?: return
        val currency = config.node("currency").string.takeIf { it == "mitty_token" || it == "orth_coin" } ?: return
        val cost = config.node("cost").int
        val data = player.playerData
        val cosmetic = Cosmetics.getCosmetic(config.node("cosmetic").string) ?: return
        if (cosmetic == user.getCosmetic(cosmetic.slot) || !cosmetic.requiresPermission() || player.hasPermission(cosmetic.permission)) return super.run(user, config, clickType)

        when (clickType) {
            ClickType.RIGHT -> when (currency) {
                "mitty_token" -> {
                    if (data.mittyTokensHeld < cost) return player.error("You don't have enough Mitty Tokens!")
                    data.mittyTokensHeld -= cost
                    player.success("You have purchased the cosmetic for $cost Mitty Tokens!")
                }
                else -> {
                    if (data.orthCoinsHeld < cost) return player.error("You don't have enough Orth Coins!")
                    data.orthCoinsHeld -= cost
                    player.success("You have purchased the cosmetic for $cost Orth Coins!")
                }
            }.apply {
                luckPerms.userManager.modifyUser(player.uniqueId) {
                    it.data().add(Node.builder(cosmetic.permission).build())
                }
                super.run(user, config, clickType)
            }
            else -> super.run(user, config, clickType)
        }
    }
}
