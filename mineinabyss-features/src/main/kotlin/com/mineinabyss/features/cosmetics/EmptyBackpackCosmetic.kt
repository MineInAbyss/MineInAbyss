package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType
import me.lojosho.shaded.configurate.CommentedConfigurationNode

object EmptyBackpackCosmetic : CosmeticBackpackType(EmptyBackpackCosmetic.ID, CommentedConfigurationNode.root().apply {
    node("slot").set(CosmeticSlot.BACKPACK.name)
    node("item", "material").set("PAPER")
    node("item", "model-id").set("minecraft:empty")
    node("firstperson-item", "material").set("PAPER")
    node("firstperson-item", "model-id").set("minecraft:empty")
}) {

    const val ID = "empty_backpack"
}