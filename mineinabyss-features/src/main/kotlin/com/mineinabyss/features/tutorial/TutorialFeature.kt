package com.mineinabyss.features.tutorial

import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.Bukkit
import org.bukkit.entity.Entity

class TutorialFeature : Feature() {

    private fun spawnTutorialEntities() {
        tutorial.tutorialEntities.forEach(TutorialEntity::spawn)
    }

    private fun setTutorialContext() {
        DI.remove<TutorialContext>()
        DI.add<TutorialContext>(object : TutorialContext {
            override val tutorialEntities by config<List<TutorialEntity>>("tutorialEntities", abyss.dataPath, listOf())
        })
    }

    override fun FeatureDSL.enable() {
        setTutorialContext()
        spawnTutorialEntities()

        plugin.listeners(TutorialListener())

        mainCommand {
            "tutorial"(desc = "Opens the tutorial") {
                "reload" {
                    action {
                        Bukkit.getWorlds().map { it.entities }.flatten()
                            .filter { it.toGearyOrNull()?.has<TutorialEntity>() == true }.forEach(Entity::remove)
                        setTutorialContext()
                        spawnTutorialEntities()
                        sender.success("Tutorial reloaded")
                    }
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("tutorial").filter { it.startsWith(args[0]) }
                2 -> if (args.first() == "tutorial") listOf("reload").filter { it.startsWith(args[1]) } else listOf()
                else -> emptyList()
            }
        }
    }

}
