package com.mineinabyss.features.tutorial

import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.TextDisplay

class TutorialFeature : Feature() {

    private fun spawnTutorialEntities() {
        tutorial.tutorialEntities.values.flatten().forEach(TutorialEntity::spawn)
    }

    private fun setTutorialContext() {
        DI.remove<TutorialContext>()
        DI.add<TutorialContext>(object : TutorialContext {
            val tutorial by config<Tutorial>("tutorial", abyss.dataPath, Tutorial())
            override val firstJoinLocation: Location? = tutorial.firstJoinLocation
            override val tutorialEntities: Long2ObjectOpenHashMap<ObjectArrayList<TutorialEntity>> =
                tutorial.tutorialEntities.groupByTo(Long2ObjectOpenHashMap()) {
                    Chunk.getChunkKey(it.location)
                }.mapValuesTo(Long2ObjectOpenHashMap()) { (_, entities) ->
                    ObjectArrayList(entities)
                }

            override val entry: TutorialRegion = tutorial.start
            override val exit: TutorialRegion = tutorial.end
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
                        Bukkit.getWorlds().flatMap { it.entities }
                            .filter { it is TextDisplay && it.toGearyOrNull()?.has<TutorialEntity>() == true }
                            .forEach(Entity::remove)
                        setTutorialContext()
                        spawnTutorialEntities()
                        sender.success("Tutorial reloaded")
                    }
                }
                "save" {
                    action {
                        Bukkit.getWorlds().flatMap { it.entities.filterIsInstance<TextDisplay>() }
                            .mapNotNull { it to (it.toGearyOrNull()?.get<TutorialEntity>() ?: return@mapNotNull null) }
                            .map { (entity, tutorial) ->
                                tutorial.copy(
                                    location = entity.location,
                                    lineWidth = entity.lineWidth,
                                    textOpacity = entity.textOpacity,
                                    backgroundColor = entity.backgroundColor ?: tutorial.backgroundColor,
                                    shadow = entity.isShadowed,
                                    alignment = entity.alignment,
                                    billboard = entity.billboard,
                                    scale = entity.transformation.scale,
                                    leftRotation = entity.transformation.leftRotation,
                                    rightRotation = entity.transformation.rightRotation,
                                )
                            }.let { config<Tutorial>("tutorial", abyss.dataPath, Tutorial(tutorial.firstJoinLocation, it, tutorial.entry, tutorial.exit)).write(Tutorial(tutorial.firstJoinLocation, it, tutorial.entry, tutorial.exit)) }

                        setTutorialContext()
                        sender.success("Successfully saved tutorial-entities")
                    }
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("tutorial").filter { it.startsWith(args[0]) }
                2 -> if (args.first() == "tutorial") listOf(
                    "reload",
                    "save"
                ).filter { it.startsWith(args[1]) } else listOf()

                else -> emptyList()
            }
        }
    }

}
