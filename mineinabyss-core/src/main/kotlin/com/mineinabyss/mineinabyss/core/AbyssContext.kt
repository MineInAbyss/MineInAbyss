package com.mineinabyss.mineinabyss.core

import com.mineinabyss.geary.addon.GearyAddon
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.plugin.getService
import github.scarsz.discordsrv.DiscordSRV
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.Database

/** A reference to the MineInAbyss plugin */
val mineInAbyss: MineInAbyssPlugin by lazy { Bukkit.getPluginManager().getPlugin("MineInAbyss") as MineInAbyssPlugin }
val discordSRV: DiscordSRV by lazy { Bukkit.getPluginManager().getPlugin("DiscordSRV") as DiscordSRV }

interface AbyssContext {
    companion object : AbyssContext by getService()
    val isChattyLoaded: Boolean
        get() = mineInAbyss.server.pluginManager.isPluginEnabled("chatty")
    val isGSitLoaded: Boolean
        get() = mineInAbyss.server.pluginManager.isPluginEnabled("GSit")
    val econ: Economy?

    val addonScope: GearyAddon
    val miaSubcommands: MutableList<Command.() -> Unit>
    val tabCompletions: MutableList<MineInAbyssPlugin.TabCompletion.() -> List<String>?>
    val commandExecutor: IdofrontCommandExecutor
    val db: Database
}
