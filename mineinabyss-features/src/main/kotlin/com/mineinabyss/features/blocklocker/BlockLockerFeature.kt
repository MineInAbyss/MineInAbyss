package com.mineinabyss.features.blocklocker

import com.mineinabyss.components.blocklocker.BlockLockerDebug
import com.mineinabyss.components.blocklocker.BlockLockerLock
import com.mineinabyss.features.helpers.BlockLockerHelpers
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.commands.arguments.offlinePlayerArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.unregisterListeners
import org.bukkit.FluidCollisionMode
import org.bukkit.block.TileState

class BlockLockerFeature: Feature() {

    private val listeners = listOf(BlockLockerListener(), DebugListener())

    override fun FeatureDSL.enable() {
        plugin.listeners(*listeners.toTypedArray())

        geary.createBlockLockerDebugTextSystem()

        mainCommand {
            "blocklocker" {
                "add" {
                    val player by offlinePlayerArg()
                    playerAction {
                        //if (!player.hasPlayedBefore()) return@playerAction
                        val targetBlock = this.player.getTargetBlockExact(5, FluidCollisionMode.NEVER) ?: return@playerAction sender.error("No container within distance...")
                        val tileState = targetBlock.state as? TileState ?: return@playerAction sender.error("TargetBlock was not lockable container...")
                        val lock = BlockLockerHelpers.blockLockerLock(targetBlock) ?: BlockLockerLock(this.player.uniqueId)

                        lock.allowedPlayers += player.uniqueId
                        tileState.persistentDataContainer.encode(lock)
                        tileState.update()

                        sender.success("Added ${player.name} to locked container!")
                    }
                }
                "remove" {
                    val player by offlinePlayerArg()
                    playerAction {
                        //if (!player.hasPlayedBefore()) return@playerAction
                        val targetBlock = this.player.getTargetBlockExact(5, FluidCollisionMode.NEVER) ?: return@playerAction
                        val tileState = targetBlock.state as? TileState ?: return@playerAction

                        tileState.persistentDataContainer.decode<BlockLockerLock>()?.allowedPlayers?.remove(player.uniqueId)
                            ?: return@playerAction sender.success("No locked container within distance...")

                        tileState.update()
                        sender.success("Removed ${player.name} from locked container!")
                    }
                }
                "toggle" {
                    playerAction {
                        with(player.toGeary()) {
                            if (has<BlockLockerDebug>()) remove<BlockLockerDebug>()
                            else add<BlockLockerDebug>()
                            player.success("Toggled BlockLocker debug <gold>${if (has<BlockLockerDebug>()) "on" else "off"}")
                        }
                    }
                }
            }
        }
    }

    override fun FeatureDSL.disable() {
        plugin.unregisterListeners(*listeners.toTypedArray())
    }
}