package com.derongan.minecraft.mineinabyss

import com.mineinabyss.idofront.plugin.getPlugin

/**
 * A reference to the MineInAbyss plugin
 */
val mineInAbyss: MineInAbyss by lazy { getPlugin() }

inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): T? {
    return enumValues<T>().find { it.name.lowercase() == name.lowercase() }
}