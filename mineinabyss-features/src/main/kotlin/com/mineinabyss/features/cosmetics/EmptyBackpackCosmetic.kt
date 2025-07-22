package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType
import com.mineinabyss.idofront.resourcepacks.ResourcePacks
import me.lojosho.shaded.configurate.CommentedConfigurationNode

object EmptyBackpackCosmetic : CosmeticBackpackType(EmptyBackpackCosmetic.ID, CommentedConfigurationNode.root().apply {
    node("slot").set(CosmeticSlot.BACKPACK.name)
    node("item", "material").set("PAPER")
    node("item", "model-id").set(ResourcePacks.EMPTY_MODEL.asString())
    node("firstperson-item", "material").set("PAPER")
    node("firstperson-item", "model-id").set(ResourcePacks.EMPTY_MODEL.asString())
}) {

    const val ID = "empty_backpack"
}