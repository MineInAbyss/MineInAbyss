package com.mineinabyss.features.lootcrates

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class CustomLootEditor(val crate: ItemDisplay) : InventoryHolder {
    private val inventory = Bukkit.createInventory(this, 27, Component.text("Custom Loot")).apply {
        crate.customLoot.take(size).forEachIndexed(::setItem)
    }

    override fun getInventory(): Inventory = inventory
}
