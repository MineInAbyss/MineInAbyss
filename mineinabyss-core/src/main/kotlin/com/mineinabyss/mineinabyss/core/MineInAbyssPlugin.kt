package com.mineinabyss.mineinabyss.core

import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.entrypoint.CommandDSLEntrypoint
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

abstract class MineInAbyssPlugin : JavaPlugin() {
    fun CommandDSLEntrypoint.mineinabyss(run: Command.() -> Unit) {
        abyss.miaSubcommands += run
    }

    fun CommandDSLEntrypoint.tabCompletion(completion: TabCompletion.() -> List<String>?) {
        abyss.tabCompletions += completion
    }

    class TabCompletion(
        val sender: CommandSender,
        val command: org.bukkit.command.Command,
        val alias: String,
        val args: Array<String>
    )

    inline fun commands(run: CommandDSLEntrypoint.() -> Unit) {
        abyss.commandExecutor.commands.apply(run)
    }
}
