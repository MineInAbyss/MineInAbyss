package com.derongan.minecraft.mineinabyss.systems

import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.minecraft.components.Soulbound
import com.mineinabyss.idofront.messaging.broadcastVal
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object SoulSystem : TickingSystem() {
    private val QueryResult.soul by get<Soulbound>()
    private val QueryResult.item by get<ItemStack>()

    override fun QueryResult.tick() {
        entity.parent?.with { player: Player ->
            if (soul.owner != player.uniqueId && item.containsEnchantment(CustomEnchants.SOULBOUND)) {
                "remove enchant".broadcastVal()
                item.removeEnchantment(CustomEnchants.SOULBOUND)
            }
            if (soul.owner == player.uniqueId && !item.containsEnchantment(CustomEnchants.SOULBOUND)) {
                "add enchant".broadcastVal()
                item.addCustomEnchant(CustomEnchants.SOULBOUND, 1, Component.text(" to ").append(player.displayName()))
            }

//            item.lore()?.contains(CustomEnchants.SOULBOUND.displayName(item.getEnchantmentLevel(CustomEnchants.SOULBOUND))).broadcastVal("Contains enchantment lore?: ")
        }
    }
}