package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType
import com.hibiscusmc.hmccosmetics.user.CosmeticUser
import com.mineinabyss.features.helpers.equipWhistleCosmetic
import me.lojosho.shaded.configurate.ConfigurationNode

class MiaCosmeticBackpackType(id: String, config: ConfigurationNode, val isWhistlesEnabled: Boolean) : CosmeticBackpackType(id, config) {

    // Since Slot is originally CUSTOM, we change it here to BACKPACK
    init {
        this.slot = CosmeticSlot.BACKPACK
    }

    override fun update(user: CosmeticUser) {
        super.update(user)
        if (isWhistlesEnabled) user.equipWhistleCosmetic()
    }
}