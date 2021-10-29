package com.mineinabyss.pins

import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.mineinabyss.core.mineInAbyss

@ExperimentalCommandDSL
object GUICommandExecutor : IdofrontCommandExecutor() {
    override val commands = commands(mineInAbyss) {
        ("mineinabyss" / "mia") command@{
        }
    }
}
