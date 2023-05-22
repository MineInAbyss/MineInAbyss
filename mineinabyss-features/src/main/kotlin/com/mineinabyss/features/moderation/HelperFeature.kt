package com.mineinabyss.features.moderation

import com.mineinabyss.components.moderation.HelperInfo
import com.mineinabyss.components.moderation.OldLocSerializer
import com.mineinabyss.components.moderation.helperMode
import com.mineinabyss.components.moderation.isInHelperMode
import com.mineinabyss.components.playerData
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("helper")
class HelperFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        listeners(HelperListener())

        commands {
            mineinabyss {
                "helper" {
                    playerAction {
                        val player = sender as? Player ?: return@playerAction
                        if (luckPerms.userManager.getUser(player.uniqueId)?.primaryGroup != "helper") {
                            player.error("This command is only for helper-rank players!")
                        } else if (!player.isInHelperMode)
                            player.enterHelperMode()
                        else player.exitHelperMode()
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("helper").filter { it.startsWith(args[0]) }
                    else -> null
                }
            }
        }
    }

    //TODO Toggle cosmetics?
    fun Player.enterHelperMode() {
        if (isInHelperMode) return
        toGeary().setPersisting(HelperInfo(
            OldLocSerializer(location.world.uid, location.x, location.y, location.z, location.yaw, location.pitch), gameMode,
            inventory.contents.map { it ?: ItemStack(Material.AIR) }))
        gameMode = GameMode.SPECTATOR
        inventory.clear()
        this.playerData.isAffectedByCurse = false
        success("Entering Helper-Mode")
    }

    fun Player.exitHelperMode() {
        if (!isInHelperMode) return
        gameMode = helperMode!!.oldGameMode
        inventory.clear()
        inventory.contents = helperMode!!.inventory.map { it }.toTypedArray()
        this.playerData.isAffectedByCurse = true
        teleport(helperMode!!.oldLocation.toLocation())
        toGeary().remove<HelperInfo>()
        error("Exited Helper-Mode")
    }
}
