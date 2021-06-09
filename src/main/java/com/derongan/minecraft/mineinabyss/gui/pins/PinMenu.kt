package com.derongan.minecraft.mineinabyss.gui.pins

import com.derongan.minecraft.guiy.gui.FillableElement
import com.derongan.minecraft.guiy.gui.GuiHolder
import com.derongan.minecraft.guiy.helpers.toCell
import com.derongan.minecraft.guiy.kotlin_dsl.guiyLayout
import com.derongan.minecraft.guiy.kotlin_dsl.setElement
import com.derongan.minecraft.mineinabyss.ecs.components.pins.ActivePins
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.minecraft.access.geary
import com.mineinabyss.looty.ecs.components.LootyType
import de.erethon.headlib.HeadLib
import org.bukkit.entity.Player

class PinMenu(
    private val player: Player
) : GuiHolder(5, "Active Pins", mineInAbyss) {
    val gearyPlayer = geary(player)

    init {
        setElement(buildMain())
    }

    private fun buildMain() = guiyLayout {
        val pins = gearyPlayer.get<ActivePins>() ?: return@guiyLayout

        setElement(0, 0, FillableElement(4, 9)) {
            addAll(pins.mapNotNull { it.toCell() })
        }
    }

    fun PrefabKey.toCell() = toEntity()?.get<LootyType>()?.createItem()?.toCell()
}
