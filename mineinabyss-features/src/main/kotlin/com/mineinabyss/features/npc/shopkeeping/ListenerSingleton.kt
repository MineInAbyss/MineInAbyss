package com.mineinabyss.features.npc.shopkeeping

import com.mineinabyss.features.npc.NpcEntity
import com.mineinabyss.features.npc.NpcsConfig

object ListenerSingleton {
    var sgl: NpcsConfig? = null
    var bstgth: MutableMap<Long, List<NpcEntity>> = mutableMapOf()
}