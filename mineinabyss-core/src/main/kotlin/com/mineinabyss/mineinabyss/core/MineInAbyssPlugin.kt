package com.mineinabyss.mineinabyss.core

import com.mineinabyss.geary.minecraft.dsl.GearyAddon
import com.mineinabyss.idofront.commands.CommandHolder
import org.bukkit.plugin.java.JavaPlugin

abstract class MineInAbyssPlugin : JavaPlugin() {
}

inline fun MineInAbyssPlugin.geary(run: GearyAddon.() -> Unit) {
    AbyssContext.addonScope.apply(run)
}

inline fun MineInAbyssPlugin.commands(run: CommandHolder.() -> Unit) {
    AbyssContext.commandExecutor.commands.apply(run)
}
