package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu
import com.hibiscusmc.hmccosmetics.gui.type.types.TypeCosmetic
import com.hibiscusmc.hmccosmetics.user.CosmeticUser
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import me.lojosho.shaded.configurate.ConfigurationNode
import net.luckperms.api.node.Node
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType


class TypeMiaCosmetic : TypeCosmetic("mia_cosmetic") {

    override fun run(player: Player, holder: CosmeticHolder, config: ConfigurationNode, clickType: ClickType) {
        val user = holder as CosmeticUser
        val currency = config.node("currency").string.takeIf { it == "mitty_token" || it == "orth_coin" } ?: return
        val cost = config.node("cost").int
        val cosmetic = Cosmetics.getCosmetic(config.node("cosmetic").string) ?: return
        val currentCosmetic = user.getCosmetic(cosmetic.slot)

        when (clickType) {
            ClickType.LEFT -> when {
                cosmetic == currentCosmetic -> user.removeCosmeticSlot(cosmetic.slot)
                user.canEquipCosmetic(cosmetic) -> {
                    user.removeCosmeticSlot(cosmetic.slot)
                    user.addCosmetic(cosmetic)
                    if (cosmetic.requiresPermission() && !player.hasPermission(cosmetic.permission))
                        player.closeInventory()
                }
            }

            ClickType.RIGHT -> when {
                cosmetic.isDyeable && (cosmetic == currentCosmetic || user.canEquipCosmetic(cosmetic, true)) ->
                    DyeMenu.openMenu(user, cosmetic)
                cosmetic.requiresPermission() && !player.hasPermission(cosmetic.permission) -> player.editPlayerData {
                    when (currency) {
                        "mitty_token" -> {
                            if (mittyTokensHeld < cost) return player.error("You don't have enough :cosmetic_mitty_token:!")
                            mittyTokensHeld -= cost
                            player.success("You have purchased the cosmetic for $cost :cosmetic_mitty_token:!")
                        }

                        else -> {
                            if (orthCoinsHeld < cost) return player.error("You don't have enough :cosmetic_orth_coin:!")
                            orthCoinsHeld -= cost
                            player.success("You have purchased the cosmetic for $cost :cosmetic_orth_coin:!")
                        }
                    }.apply {
                        luckPerms.userManager.modifyUser(player.uniqueId) {
                            it.data().add(Node.builder(cosmetic.permission).build())
                        }
                        super.run(player, holder, config, clickType)
                    }
                }
            }

            else -> super.run(player, holder, config, clickType)
        }
    }
}
