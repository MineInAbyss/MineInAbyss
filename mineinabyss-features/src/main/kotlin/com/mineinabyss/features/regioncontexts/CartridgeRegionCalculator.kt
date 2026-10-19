package com.mineinabyss.features.regioncontexts

import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import org.bukkit.entity.Player

class CartridgeRegionCalculator(private val regions: CartridgeRegionLookup) : ContextCalculator<Player> {
    override fun calculate(target: Player, consumer: ContextConsumer) {
        regions.namesAt(target.location).forEach { consumer.accept(KEY, it) }
    }

    companion object {
        const val KEY = "cartridge:region"
    }
}
