package com.derongan.minecraft.mineinabyss

import com.derongan.minecraft.guiy.GuiListener
import com.derongan.minecraft.mineinabyss.ascension.AscensionListener
import com.derongan.minecraft.mineinabyss.commands.AscensionCommandExecutor
import com.derongan.minecraft.mineinabyss.commands.GUICommandExecutor
import com.derongan.minecraft.mineinabyss.ecs.components.pins.ActivePins
import com.derongan.minecraft.mineinabyss.ecs.systems.OrthReturnSystem
import com.derongan.minecraft.mineinabyss.ecs.systems.PinActivatorSystem
import com.derongan.minecraft.mineinabyss.ecs.systems.PinDropperSystem
import com.derongan.minecraft.mineinabyss.player.PlayerListener
import com.derongan.minecraft.mineinabyss.services.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManagerImpl
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.minecraft.dsl.attachToGeary
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.getServiceOrNull
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import kotlinx.serialization.InternalSerializationApi
import net.milkbowl.vault.economy.Economy
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class MineInAbyss : JavaPlugin() {
    @InternalSerializationApi
    @ExperimentalCommandDSL
    override fun onEnable() {
        // Plugin startup logic
        logger.info("On enable has been called")

        //Vault setup
        if (econ == null) {
            logger.severe("Disabled due to no Vault dependency found!")
            server.pluginManager.disablePlugin(this)
            return
        }

        //Geary setup
        //TODO make a serviceRegistered function idofront
        if (getServiceOrNull<Engine>(plugin = "Geary") != null) {
            attachToGeary {
                autoscanComponents()
                autoscanActions()

                systems(
                    PinActivatorSystem()
                )
                bukkitEntityAccess {
                    onEntityRegister<Player> {
                        //TODO Kotlin bug (?) Sees type as Unit unless specified explicitly
                        getOrSetPersisting<ActivePins> { ActivePins() }
                    }
                }
            }
        } else logger.warning("Geary service not found! No items have been added!")

        // Remove recipes if already loaded. This way changes will take effect properly.

        registerService<AbyssWorldManager>(AbyssWorldManagerImpl())
        registerEvents(
            GuiListener(this),
            PlayerListener,
            AscensionListener,
            PinDropperSystem(),
            OrthReturnSystem
        )


        //register command executors
        AscensionCommandExecutor
        GUICommandExecutor
    }

    companion object {
        @JvmStatic
        val econ by lazy { getServiceOrNull<Economy>("Vault") }
    }
}
