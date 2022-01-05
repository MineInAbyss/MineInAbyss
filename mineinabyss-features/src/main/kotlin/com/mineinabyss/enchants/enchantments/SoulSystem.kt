package com.mineinabyss.enchants.enchantments

import com.mineinabyss.enchants.CustomEnchants
import com.mineinabyss.enchants.addCustomEnchant
import com.mineinabyss.enchants.updateEnchantmentLore
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.minecraft.components.Soulbound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SoulSystem : TickingSystem() {
    private val ResultScope.soul by get<Soulbound>()
    private val ResultScope.item by get<ItemStack>()

    override fun ResultScope.tick() {
        //TODO generalize for all enchants
        entity.parent?.with { player: Player ->
            if (soul.owner != player.uniqueId && item.containsEnchantment(CustomEnchants.SOULBOUND)) {
                item.removeEnchantment(CustomEnchants.SOULBOUND)
            }
            if (soul.owner == player.uniqueId && !item.containsEnchantment(CustomEnchants.SOULBOUND)) {
                item.addCustomEnchant(
                    CustomEnchants.SOULBOUND,
                    1,
                    "to ${soul.ownerName}"
                )
            }

            item.updateEnchantmentLore(
                CustomEnchants.SOULBOUND,
                1,
                "to ${soul.ownerName}"
            )
        }
    }
}
