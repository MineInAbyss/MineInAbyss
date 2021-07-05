package com.derongan.minecraft.mineinabyss.gui.pins

import com.derongan.minecraft.guiy.gui.GuiHolder
import com.derongan.minecraft.guiy.helpers.toCell
import com.derongan.minecraft.guiy.kotlin_dsl.button
import com.derongan.minecraft.guiy.kotlin_dsl.guiyLayout
import com.derongan.minecraft.mineinabyss.ecs.components.pins.AbyssalPin
import com.derongan.minecraft.mineinabyss.ecs.components.pins.ActivePins
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.minecraft.access.geary
import com.mineinabyss.looty.ecs.components.LootyType
import org.bukkit.entity.Player

class AbyssalPinSelectionMenu(
    private val player: Player
) : GuiHolder(3, "Select a Pin", mineInAbyss) {
    val gearyPlayer = geary(player)

    private object AbyssalPinsQuery : Query() {
        val QueryResult.prefab by get<PrefabKey>()
        val QueryResult.pin by get<AbyssalPin>()
    }

    init {
        setElement(buildMain())
    }

    private fun buildMain() = guiyLayout {
        val activePins = gearyPlayer.get<ActivePins>() ?: return@guiyLayout
        //TODO efficiently get all prefabs with AbyssalPin components
        AbyssalPinsQuery.apply {
            asSequence()
                .map { it.prefab }
                .filter { it !in activePins }
                .shuffled()
                .take(3)
                .mapNotNull {
                    it to (it.toEntity()?.get<LootyType>()?.createItem() ?: return@mapNotNull null)
                }
                .forEachIndexed { i, (key, item) ->
                    button(i * 2 + 2, 1, item.toCell()) {
                        activePins += key
                        player.closeInventory()
                    }
                }
        }
    }
}
