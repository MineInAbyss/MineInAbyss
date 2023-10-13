package com.mineinabyss.features.tutorial

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.TextDisplay

@Serializable
@SerialName("tutorial")
class TutorialFeature : AbyssFeature {
    private fun TextDisplay.trackEntity() {
        toGearyOrNull()?.add<TutorialEntity>() ?: run {
            abyss.plugin.launch {
                delay(10.ticks)
                trackEntity()
            }
        }
    }
    private fun spawnTutorialEntities() {
        for (entity in tutorial.tutorialEntities) {
            val text = entity.location.world.spawn(entity.location, TextDisplay::class.java) { textDisplay ->
                textDisplay.text(entity.text.miniMsg())
                textDisplay.billboard = entity.billboard
                textDisplay.alignment = entity.alignment
                textDisplay.isShadowed = entity.shadow
                textDisplay.backgroundColor = entity.backgroundColor
                entity.viewRange?.let { textDisplay.viewRange = it }
                textDisplay.transformation = textDisplay.transformation.apply { scale.set(entity.scale) }

                textDisplay.isPersistent = false
            }
            text.trackEntity()
        }
    }

    private fun setTutorialContext() {
        DI.remove<TutorialContext>()
        DI.add<TutorialContext>(object : TutorialContext {
            override val tutorialEntities by config<List<TutorialEntity>>("tutorialEntities", abyss.dataPath, listOf())
        })
    }

    override fun MineInAbyssPlugin.enableFeature() {

        setTutorialContext()
        spawnTutorialEntities()

        commands {
            mineinabyss {
                "tutorial"(desc = "Opens the tutorial") {
                    "reload" {
                        action {
                            Bukkit.getWorlds().map { it.entities }.flatten().filter { it.toGearyOrNull()?.has<TutorialEntity>() == true }.forEach(Entity::remove)
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
}
