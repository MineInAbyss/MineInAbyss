package com.mineinabyss.enchants.enchantments

//import com.mineinabyss.geary.papermc.components.Soulbound
import com.mineinabyss.geary.systems.GearyListener

class SoulSystem : GearyListener() {
    /*private val TargetScope.soul by onSet<Soulbound>()
    private val TargetScope.item by onFirstSet<ItemStack>()

    @Handler
    fun TargetScope.updateItemLore() {
        entity.parent?.with { player: Player ->
            val ownerName = soul.owner.toPlayer()?.name ?: "No owner"
            if (soul.owner != player.uniqueId && CustomEnchants.SOULBOUND in item.enchantments)
                item.removeEnchantment(CustomEnchants.SOULBOUND)
            else if (soul.owner == player.uniqueId && CustomEnchants.SOULBOUND !in item.enchantments)
                item.addCustomEnchant(CustomEnchants.SOULBOUND, 1, "to $ownerName")

            item.updateEnchantmentLore(CustomEnchants.SOULBOUND, 1, "to $ownerName")
        }
    }*/
}
