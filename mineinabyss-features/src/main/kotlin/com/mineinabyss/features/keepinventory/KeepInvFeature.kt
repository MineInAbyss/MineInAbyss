package com.mineinabyss.features.keepinventory

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
class KeepInvConfig {
    val enabled = true
    val keepInvInVoid: Boolean = true
}

val KeepInvFeature = module("keepinv") {
    val config = get<AbyssFeatureConfig>().keepInventory
    require(config.enabled) { "Keep inventory feature is disabled" }

    single<KeepInvConfig> { config }

    listeners(new(::KeepInvListener))
    if (abyss.isEternalFortuneLoaded) listeners(new(::KeepInvGraveListener))
}.mainCommand {
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
