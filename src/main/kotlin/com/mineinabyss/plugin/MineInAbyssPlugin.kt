package com.mineinabyss.plugin

import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.PrefabNamespaceMigrations
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.actions
import org.bukkit.plugin.java.JavaPlugin

class MineInAbyssPlugin : JavaPlugin() {

    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() = actions {
        geary {
            autoscan(
                classLoader,
                "com.mineinabyss.features",
                "com.mineinabyss.components",
                "com.mineinabyss.mineinabyss.core"
            ) {
                components()
                subClassesOf<AscensionEffect>()
            }

            on(GearyPhase.ENABLE) {
                DI.add(AbyssFeatureManager(this@MineInAbyssPlugin))
                featureManager.enable()
            }
        }

        PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")
    }

    override fun onDisable() {
        featureManager.disable()
    }
}
