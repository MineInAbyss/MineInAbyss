package com.mineinabyss.mineinabyss.core

import com.mineinabyss.geary.addon.GearyAddon
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.CommandHolder
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

abstract class MineInAbyssPlugin : JavaPlugin() {
    fun CommandHolder.mineinabyss(run: Command.() -> Unit) {
        AbyssContext.miaSubcommands += run
    }

    fun CommandHolder.tabCompletion(completion: TabCompletion.() -> List<String>?) {
        AbyssContext.tabCompletions += completion
    }

    class TabCompletion(
        val sender: CommandSender,
        val command: org.bukkit.command.Command,
        val alias: String,
        val args: Array<String>
    )
}

inline fun MineInAbyssPlugin.geary(run: GearyAddon.() -> Unit) {
    AbyssContext.addonScope.apply(run)
}

inline fun MineInAbyssPlugin.commands(run: CommandHolder.() -> Unit) {
    AbyssContext.commandExecutor.commands.apply(run)
}
