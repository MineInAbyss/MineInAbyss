package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType
import me.lojosho.shaded.configurate.CommentedConfigurationNode

object EmptyBackpackCosmetic : CosmeticBackpackType(EmptyBackpackCosmetic.ID, CommentedConfigurationNode.root().apply {
    node("slot").set(CosmeticSlot.BACKPACK.name)
    node("firstperson-item").set("")
}) {

    const val ID = "empty_backpack"
}