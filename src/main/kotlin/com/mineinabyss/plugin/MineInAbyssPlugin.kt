package com.mineinabyss.plugin

import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.datastore.PrefabNamespaceMigrations
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.di.DI
import org.bukkit.plugin.java.JavaPlugin


class MineInAbyssPlugin : JavaPlugin() {

    override fun onLoad() {
        gearyPaper.configure {
            install(createAddon("MineInAbyss", configuration = {
                autoscan(
                    classLoader,
                    "com.mineinabyss.features",
                    "com.mineinabyss.components",
                    "com.mineinabyss.mineinabyss.core"
                ) {
                    components()
                    subClassesOf<AscensionEffect>()
                }
            }))
        }

        PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")

        DI.add(AbyssFeatureManager(this@MineInAbyssPlugin))
    }

    override fun onEnable() {
        featureManager.enable()
    }

    override fun onDisable() {
        featureManager.disable()
    }
}
