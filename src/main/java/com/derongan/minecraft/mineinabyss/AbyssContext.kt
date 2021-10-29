package com.derongan.minecraft.mineinabyss

import com.mineinabyss.idofront.plugin.getServiceOrNull
import net.milkbowl.vault.economy.Economy

object AbyssContext {
    val econ by lazy { getServiceOrNull<Economy>("Vault") }
}
