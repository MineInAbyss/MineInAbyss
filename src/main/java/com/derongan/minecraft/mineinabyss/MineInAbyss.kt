package com.derongan.minecraft.mineinabyss

import com.derongan.minecraft.mineinabyss.ascension.AscensionListener
import com.derongan.minecraft.mineinabyss.commands.AscensionCommandExecutor
import com.derongan.minecraft.mineinabyss.commands.GUICommandExecutor
import com.derongan.minecraft.mineinabyss.ecs.components.pins.ActivePins
import com.derongan.minecraft.mineinabyss.ecs.systems.OrthReturnSystem
import com.derongan.minecraft.mineinabyss.ecs.systems.PinActivatorSystem
import com.derongan.minecraft.mineinabyss.ecs.systems.PinDropperSystem
import com.derongan.minecraft.mineinabyss.player.PlayerListener
import com.derongan.minecraft.mineinabyss.services.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.systems.CustomEnchants
import com.derongan.minecraft.mineinabyss.systems.FrostAspectListener
import com.derongan.minecraft.mineinabyss.systems.SoulSystem
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManagerImpl
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.minecraft.dsl.gearyAddon
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.getServiceOrNull
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.mineinabyss.idofront.slimjar.IdofrontSlimjar
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

@ExperimentalCommandDSL
class MineInAbyss : JavaPlugin() {
    override fun onLoad() {
        IdofrontSlimjar.loadToLibraryLoader(this)
    }

    override fun onEnable() {
        CustomEnchants.register()

        //Vault setup
        if (AbyssContext.econ == null) {
            logger.severe("Disabled due to no Vault dependency found!")
            server.pluginManager.disablePlugin(this)
            return
        }

        //Geary setup
        //TODO make a serviceRegistered function idofront
        if (getServiceOrNull<Engine>(plugin = "Geary") != null) {
            gearyAddon {
                autoscanAll()
                systems(
                    SoulSystem,
                    PinActivatorSystem(),
                )
                bukkitEntityAssociations {
                    onEntityRegister<Player> {
                        //TODO kotlin bug, removing this defaults it to Unit but only sometimes
                        getOrSetPersisting<ActivePins> { ActivePins() }
                    }
                }
            }
        } else logger.warning("Geary service not found! No items have been added!")

        // Remove recipes if already loaded. This way changes will take effect properly.

        registerService<AbyssWorldManager>(AbyssWorldManagerImpl())
        registerEvents(
            PlayerListener,
            AscensionListener,
            FrostAspectListener,
            AscensionListener,
            PinDropperSystem(),
            OrthReturnSystem
        )

        //register command executors
        AscensionCommandExecutor
        GUICommandExecutor
    }
}
