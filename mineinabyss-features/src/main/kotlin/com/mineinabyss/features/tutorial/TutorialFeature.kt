package com.mineinabyss.features.tutorial

import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.success
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.entity.Entity
import org.bukkit.entity.TextDisplay
import org.koin.core.module.dsl.scopedOf

val TutorialFeature: Feature = feature("tutorial") {
    dependsOn {
        plugins("DeeperWorld")
    }

    scopedModule {
        scoped<Tutorial> { config<Tutorial>("tutorial", abyss.dataPath, Tutorial()).getOrLoad() }
        scoped<TutorialContext> {
            val tutorial = get<Tutorial>()

            TutorialContext(
                firstJoinLocation = tutorial.firstJoinLocation,
                tutorialEntities = tutorial.tutorialEntities
                    .groupBy { Chunk.getChunkKey(it.location) }
                    .mapValuesTo(Long2ObjectOpenHashMap()) { (_, entities) -> ObjectArrayList(entities) },
                entry = tutorial.start,
                exit = tutorial.end,
            )
        }
        scopedOf(::TutorialListener)
    }

    onEnable {
        listeners(get<TutorialListener>())
        get<TutorialContext>().spawnTutorialEntities()
    }

    mainCommand {
        "tutorial" {
            "reload" {
                executes {
                    Bukkit.getWorlds().flatMap { it.entities }
                        .filter { it is TextDisplay && it.toGearyOrNull()?.has<TutorialEntity>() == true }
                        .forEach(Entity::remove)
                    get<TutorialContext>().spawnTutorialEntities()
                    sender.success("Tutorial reloaded")
                }
            }
        }
        "save" {
            executes {
                val context = get<TutorialContext>()
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
                    }.let { config<Tutorial>("tutorial", abyss.dataPath, Tutorial(context.firstJoinLocation, it, context.entry, context.exit)).write(Tutorial(context.firstJoinLocation, it, context.entry, context.exit)) }

                featureManager.reload(TutorialFeature)
                sender.success("Successfully saved tutorial-entities")
            }
        }
    }
}
