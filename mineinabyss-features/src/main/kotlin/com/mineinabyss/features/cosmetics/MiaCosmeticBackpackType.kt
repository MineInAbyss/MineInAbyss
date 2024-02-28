package com.mineinabyss.features.cosmetics

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType
import com.hibiscusmc.hmccosmetics.user.CosmeticUser
import com.mineinabyss.features.helpers.cosmeticUser
import com.mineinabyss.features.helpers.equipWhistleCosmetic
import com.mineinabyss.features.helpers.layerWhistleCosmetic
import com.mineinabyss.idofront.messaging.broadcast
import me.lojosho.hibiscuscommons.util.packets.PacketManager
import me.lojosho.shaded.configurate.ConfigurationNode
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

class MiaCosmeticBackpackType(id: String, config: ConfigurationNode) : CosmeticBackpackType(id, config) {

    // Since Slot is originally CUSTOM, we change it here to BACKPACK
    init {
        this.slot = CosmeticSlot.BACKPACK
    }

    override fun update(user: CosmeticUser) {
        super.update(user)
        user.equipWhistleCosmetic()
    }
}