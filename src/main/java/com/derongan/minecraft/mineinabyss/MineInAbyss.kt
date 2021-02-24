package com.derongan.minecraft.mineinabyss

import com.derongan.minecraft.guiy.GuiListener
import com.derongan.minecraft.mineinabyss.ascension.AscensionListener
import com.derongan.minecraft.mineinabyss.commands.AscensionCommandExecutor
import com.derongan.minecraft.mineinabyss.commands.GUICommandExecutor
import com.derongan.minecraft.mineinabyss.configuration.PlayerDataConfig
import com.derongan.minecraft.mineinabyss.player.PlayerListener
import com.derongan.minecraft.mineinabyss.services.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManagerImpl
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.types.GearyEntityType
import com.mineinabyss.geary.minecraft.dsl.attachToGeary
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.getServiceOrNull
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import kotlinx.serialization.InternalSerializationApi
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin

class MineInAbyss : JavaPlugin() {
    @InternalSerializationApi
    @ExperimentalCommandDSL
    override fun onEnable() {
        // Plugin startup logic
        logger.info("On enable has been called")

        AbyssContext
        PlayerDataConfig

        //Vault setup
        if (econ == null) {
            logger.severe("Disabled due to no Vault dependency found!")
            server.pluginManager.disablePlugin(this)
            return
        }

        //Geary setup
        //TODO make a serviceRegistered function idofront
        if (getServiceOrNull<Engine>(plugin = "Geary") != null) {
            attachToGeary<GearyEntityType> {
                autoscanComponents()
                autoscanActions()
            }
        } else logger.warning("Geary service not found! No items have been added!")

        // Remove recipes if already loaded. This way changes will take effect properly.

        registerService<AbyssWorldManager>(AbyssWorldManagerImpl())
        registerEvents(
            GuiListener(this),
            PlayerListener,
            AscensionListener
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
