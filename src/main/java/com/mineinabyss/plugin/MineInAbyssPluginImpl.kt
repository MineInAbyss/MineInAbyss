package com.mineinabyss.plugin

import com.mineinabyss.enchants.FrostAspectListener
import com.mineinabyss.exp.ExpListener
import com.mineinabyss.geary.minecraft.dsl.GearyAddon
import com.mineinabyss.geary.minecraft.dsl.gearyAddon
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.plugin.getServiceOrNull
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.mineinabyss.idofront.slimjar.IdofrontSlimjar
import com.mineinabyss.mineinabyss.core.*
import net.milkbowl.vault.economy.Economy

@ExperimentalCommandDSL
class MineInAbyssPluginImpl : MineInAbyssPlugin() {
    override fun onLoad() {
        IdofrontSlimjar.loadToLibraryLoader(this)
    }

    override fun onEnable() {
        gearyAddon {

            registerService<AbyssContext>(object : AbyssContext {
                override val econ = getServiceOrNull<Economy>("Vault")
                override val addonScope: GearyAddon = this@gearyAddon
                override val commandExecutor = object : IdofrontCommandExecutor() {
                    override val commands = commands(this@MineInAbyssPluginImpl) { }
                }
            })

            autoscan<AbyssFeature>(runNow = true)
            autoscanAll()
        }

        val config = MIAConfigImpl()
        config.load()
        registerService<MIAConfig>(config)

        registerService<AbyssWorldManager>(AbyssWorldManagerImpl())
        registerEvents(
            ExpListener(),
            FrostAspectListener(),
        )
    }
}
