package com.mineinabyss.enchants.enchantments

import com.mineinabyss.enchants.CustomEnchants
import com.mineinabyss.enchants.addCustomEnchant
import com.mineinabyss.enchants.updateEnchantmentLore
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.papermc.components.Soulbound
import com.mineinabyss.idofront.entities.toPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SoulSystem : GearyListener() {
    private val TargetScope.soul by added<Soulbound>()
    private val TargetScope.item by added<ItemStack>()

    @Handler
    fun TargetScope.updateItemLore() {
        //TODO generalize for all enchants
        entity.parent?.with { player: Player ->
            val ownerName = soul.owner.toPlayer()?.name ?: "No owner"
            if (soul.owner != player.uniqueId && item.containsEnchantment(CustomEnchants.SOULBOUND)) {
                item.removeEnchantment(CustomEnchants.SOULBOUND)
            }
            if (soul.owner == player.uniqueId && !item.containsEnchantment(CustomEnchants.SOULBOUND)) {
                item.addCustomEnchant(
                    CustomEnchants.SOULBOUND,
                    1,
                    "to $ownerName"
                )
            }

            item.updateEnchantmentLore(
                CustomEnchants.SOULBOUND,
                1,
                "to $ownerName"
            )
        }
    }
}
