package com.derongan.minecraft.mineinabyss.systems

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.mineinabyss.components.Orthbound
import com.derongan.minecraft.mineinabyss.configuration.MIAConfig
import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.minecraft.components.Soulbound
import com.mineinabyss.geary.minecraft.store.encodeComponentsTo
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack

object SoulSystem : TickingSystem(), Listener {
    private val QueryResult.soul by get<Soulbound>()
    private val QueryResult.item by get<ItemStack>()

    override fun QueryResult.tick() {
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

    @EventHandler
    fun PlayerAscendEvent.soulbindItems() {
        if (toSection != MIAConfig.data.hubSection) return
        player.inventory.contents.filterNotNull().forEach {
            val item = it.toGearyFromUUIDOrNull() ?: return
            item.get<Orthbound>() ?: return@forEach
            item.setPersisting(Soulbound(player.uniqueId, player.name))
            item.encodeComponentsTo(it)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun PlayerDeathEvent.death() {
        val player = entity
        player.inventory.contents.filterNotNull().forEach {
            val item = it.toGearyFromUUIDOrNull() ?: return

            if (item.get<Soulbound>()?.owner == player.uniqueId) {
                if (drops.contains(it)) {
                    drops -= it
                    itemsToKeep += it
                }
            }
            return@forEach
        }
    }
}