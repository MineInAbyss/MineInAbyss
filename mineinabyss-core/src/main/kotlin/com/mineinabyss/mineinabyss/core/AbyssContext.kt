package com.mineinabyss.mineinabyss.core

import com.mineinabyss.geary.minecraft.dsl.GearyAddon
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.plugin.getService
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit

/** A reference to the MineInAbyss plugin */
val mineInAbyss: MineInAbyssPlugin by lazy { Bukkit.getPluginManager().getPlugin("MineInAbyss") as MineInAbyssPlugin }

interface AbyssContext {
    companion object : AbyssContext by getService()

    val econ: Economy?

    val addonScope: GearyAddon
    val commandExecutor: IdofrontCommandExecutor
}
