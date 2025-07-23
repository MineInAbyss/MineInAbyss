package com.mineinabyss.plugin

import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI
import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.features.cosmetics.MiACosmeticProvider
import com.mineinabyss.features.cosmetics.MiAUserProvider
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.autoscan.autoscan
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

        //runCatching {
        //    HMCCosmeticsAPI.registerCosmeticSlot("MIA_BACKPACK")
        //    HMCCosmeticsAPI.registerCosmeticUserProvider(MiAUserProvider())
        //    HMCCosmeticsAPI.registerCosmeticProvider(MiACosmeticProvider())
        //}
    }

    override fun onEnable() {
        featureManager.enable()
    }

    override fun onDisable() {
        featureManager.disable()
    }
}
