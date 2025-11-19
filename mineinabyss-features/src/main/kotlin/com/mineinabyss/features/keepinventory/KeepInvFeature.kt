package com.mineinabyss.features.keepinventory

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import org.koin.core.module.dsl.scopedOf

@Serializable
class KeepInvConfig {
    val enabled = true
    val keepInvInVoid: Boolean = true
}

val KeepInvFeature = feature("keep-inventory") {
    scopedModule {
        scoped { abyss.config.keepInventory }
        scopedOf(::KeepInvListener)
        scopedOf(::KeepInvGraveListener)
    }

    onEnable {
        listeners(get<KeepInvListener>())
        if (abyss.isEternalFortuneLoaded) listeners(get<KeepInvGraveListener>())
    }

    mainCommand {
        "keepinv" {
            description = "Commands to toggle keepinventory status"
            executes.asPlayer().args("enabled" to Args.bool()) { toggled ->
                val player = sender as Player
                player.editPlayerData { keepInvStatus = toggled }
                if (toggled) player.success("Keep Inventory enabled for ${player.name}")
                else sender.error("Keep Inventory disabled for ${player.name}")
            }
        }
    }
}
