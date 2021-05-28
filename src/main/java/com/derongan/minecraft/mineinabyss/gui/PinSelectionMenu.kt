package com.derongan.minecraft.mineinabyss.gui

import com.derongan.minecraft.guiy.gui.GuiHolder
import com.derongan.minecraft.guiy.helpers.toCell
import com.derongan.minecraft.guiy.kotlin_dsl.button
import com.derongan.minecraft.guiy.kotlin_dsl.guiyLayout
import com.derongan.minecraft.mineinabyss.ecs.components.ActivePins
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.minecraft.access.geary
import com.mineinabyss.looty.ecs.components.LootyType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PinSelectionMenu(
    private val player: Player
): GuiHolder(3, "Select a Pin", mineInAbyss) {
    val gearyPlayer = geary(player)

    init {
        setElement(buildMain())
    }

    private fun buildMain() = guiyLayout {
        val pins = gearyPlayer.get<ActivePins>() ?: return@guiyLayout
        PrefabManager.getPrefabsFor("looty")//.asSequence()
            .filter { it !in pins.active }
            .shuffled()
            .take(3)
            .mapNotNull {
                it to (it.toEntity()?.get<LootyType>()?.createItem() ?: return@mapNotNull null)
            }
            .forEachIndexed { i, (key, item) ->
                button(i * 2 + 2, 1, item.toCell()) {
                    pins.active += key
                    player.closeInventory()
                }
            }
    }
}
