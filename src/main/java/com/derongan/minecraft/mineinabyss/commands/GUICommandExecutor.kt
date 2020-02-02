package com.derongan.minecraft.mineinabyss.commands

import com.derongan.minecraft.mineinabyss.AbyssContext
import com.derongan.minecraft.mineinabyss.MineInAbyss
import com.derongan.minecraft.mineinabyss.Permissions
import com.derongan.minecraft.mineinabyss.getPlayerData
import com.derongan.minecraft.mineinabyss.gui.GondolaGUI
import com.derongan.minecraft.mineinabyss.gui.StatsGUI
import com.mineinabyss.idofront.error
import com.mineinabyss.idofront.info
import com.mineinabyss.idofront.success
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class GUICommandExecutor(private val context: AbyssContext) : CommandExecutor {
    private val leaveConfirm = ArrayList<UUID>()


    //TODO check out https://www.spigotmc.org/resources/1-13-commandapi.62353/
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        fun String.permitted(vararg labels: String) = sender.hasPermission(this) && labels.contains(command.label)

        if (sender !is Player) {
            sender.error("Only players can run this command!")
            return false
        }

        val playerData = getPlayerData(sender)

        if (Permissions.STATS.permitted(CommandLabels.STATS)) {
            StatsGUI(sender, JavaPlugin.getPlugin(MineInAbyss::class.java)).show(sender)
            return true
        } else if (Permissions.START_DESCENT.permitted(CommandLabels.START)) {
            if (playerData.isIngame) {
                sender.error("You are already ingame!\nYou can leave using /stopdescent")
                return true
            }
            GondolaGUI(sender, JavaPlugin.getPlugin(MineInAbyss::class.java)).show(sender)
            return true


        } else if (Permissions.STOP_DESCENT.permitted(CommandLabels.STOP_DESCENT)) {
            if (!playerData.isIngame) {
                sender.error("You are not currently ingame!\nStart by using /start")
            } else if (!leaveConfirm.contains(sender.uniqueId)) {
                leaveConfirm.add(sender.uniqueId)
                sender.info("&cYou are about to leave the game!!!\n" +
                        "&lYour progress will be lost&r&c, but any xp and money you earned will stay with you.\n" +
                        "Type /stopdescent again to leave")
            } else {
                leaveConfirm.remove(sender.uniqueId)
                sender.health = 0.0
            }
            return true
        } else if (Permissions.CREATE_GONDOLA_SPAWN.permitted(CommandLabels.CREATE_GONDOLA_SPAWN)) {
            val spawnLocConfig = context.configManager.startLocationCM
            val spawns = spawnLocConfig.getMapList(GondolaGUI.SPAWN_KEY)
            var displayItem = sender.inventory.itemInMainHand.clone()

            if (displayItem.type == Material.AIR)
                displayItem = ItemStack(Material.GRASS_BLOCK)

            val meta = displayItem.itemMeta
            if (meta != null) {
                if (args.isNotEmpty())
                    meta.setDisplayName(args[0])
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES) //TODO probably better to add these tags to the serialized items
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                displayItem.itemMeta = meta
            }

            val map = HashMap<String, Any>()
            map["location"] = sender.location //TODO convert these to keys
            map["display-item"] = displayItem
            if (args.size >= 2)
                map["cost"] = args[1]

            spawns.add(map)
            spawnLocConfig.set(GondolaGUI.SPAWN_KEY, spawns)
            spawnLocConfig.saveConfig()

            sender.success("Created spawn")
            return true
        }

        return false
    }
}
