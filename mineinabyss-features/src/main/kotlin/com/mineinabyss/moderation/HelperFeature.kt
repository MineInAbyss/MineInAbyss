package com.mineinabyss.moderation

import com.mineinabyss.components.moderation.HelperInfo
import com.mineinabyss.components.moderation.OldLocSerializer
import com.mineinabyss.components.moderation.helperMode
import com.mineinabyss.components.moderation.isInHelperMode
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.luckPerms
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.luckperms.api.node.Node
import org.bukkit.GameMode
import org.bukkit.entity.Player

@Serializable
@SerialName("helper")
class HelperFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(HelperListener())

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
            OldLocSerializer(location.world.uid, location.x, location.y, location.z, location.yaw, location.pitch), gameMode, inventory.contents
            ?.map { it?.toSerializable() ?: SerializableItemStack() } ?: emptyList()))
        gameMode = GameMode.SPECTATOR
        inventory.clear()
        luckPerms.userManager.getUser(this.uniqueId)?.let {
            helperPerms.forEach { node -> it.data().add(node) }
            luckPerms.userManager.saveUser(it)
        }
        success("Entering Helper-Mode")
    }

    fun Player.exitHelperMode() {
        if (!isInHelperMode) return
        gameMode = helperMode!!.oldGameMode
        inventory.clear()
        inventory.contents = helperMode!!.inventory.map { it.toItemStack() }.toTypedArray()
        luckPerms.userManager.getUser(this.uniqueId)?.let {
            helperPerms.forEach { node -> it.data().remove(node) }
            luckPerms.userManager.saveUser(it)
        }
        teleport(helperMode!!.oldLocation.toLocation())
        toGeary().remove<HelperInfo>()
        error("Exited Helper-Mode")
    }

    private val helperPerms: Set<Node>
        get() =
            setOf(
                Node.builder("essentials.teleport").build(),
                Node.builder("coreprotect.inspect").build(),
                Node.builder("coreprotect.lookup").build(),
                Node.builder("coreprotect.rollback").build(),
                Node.builder("coreprotect.restore").build(),
            )
}
