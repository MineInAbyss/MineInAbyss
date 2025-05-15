package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.user.CosmeticUser
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.equipWhistleCosmetic
import java.util.*

class MiACosmeticUser(uuid: UUID) : CosmeticUser(uuid) {

    override fun updateCosmetic(slot: CosmeticSlot) {
        super.updateCosmetic(slot)
        if (slot != CosmeticSlot.BACKPACK) return
        if (abyss.config.cosmetics.equipWhistleCosmetic) equipWhistleCosmetic()
    }

}