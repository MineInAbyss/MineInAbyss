package com.mineinabyss.features.relics

import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta


fun GearyModule.toggleStarCompassHudAction() = observe<ToggleStarCompassHud>()
    .exec(query<ItemStack>()) { (item) ->
        val item = item
        val player = entity.parent?.get<Player>() ?: return@exec
        if (entity.has<ShowStarCompassHud>()) {
            item.addUnsafeEnchantment(Enchantment.INFINITY, 1)
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            item.editItemMeta<CompassMeta> {
                lodestone = player.location.section?.centerLocation
                isLodestoneTracked = false
            }
            entity.remove<ShowStarCompassHud>()
        } else {
            entity.setPersisting(ShowStarCompassHud())
            item.removeEnchantment(Enchantment.INFINITY)
            item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
            item.editItemMeta<CompassMeta> {
                lodestone = null
                isLodestoneTracked = false
            }
        }
        entity.encodeComponentsTo(item)
    }
