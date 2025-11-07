package com.mineinabyss.features.tutorial

import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.entity.Entity
import org.bukkit.entity.TextDisplay
import org.koin.core.module.Module
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.ScopeDSL
import org.koin.dsl.module

class TutorialFeature(
    val context: TutorialContext,
    val tutorialConfig: Tutorial,
    val listener: TutorialListener,
) : Feature() {
    fun reload(): Nothing {
        TODO()
    }

    private fun spawnTutorialEntities() {
        context.tutorialEntities.values.flatten().forEach(TutorialEntity::spawn)
    }

    override fun FeatureDSL.enable() {
        spawnTutorialEntities()

        plugin.listeners(listener)

        mainCommand {
            "tutorial"(desc = "Opens the tutorial") {
                "reload" {
                    action {
                        Bukkit.getWorlds().flatMap { it.entities }
                            .filter { it is TextDisplay && it.toGearyOrNull()?.has<TutorialEntity>() == true }
                            .forEach(Entity::remove)
//                        setTutorialContext()
                        spawnTutorialEntities()
                        sender.success("Tutorial reloaded")
                        reload()
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
                            }.let { config<Tutorial>("tutorial", abyss.dataPath, Tutorial(context.firstJoinLocation, it, context.entry, context.exit)).write(Tutorial(context.firstJoinLocation, it, context.entry, context.exit)) }

                        reload()
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


    companion object : ModuleProvider {
        override fun createModule(): Module = module {
            scope<TutorialFeature> {
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
                scopedOf(::TutorialFeature)
            }
        }

        fun test() {
            feature {
                globalModule {

                }
                scopedModule {
                    scopedOf(::TutorialFeature)
                }


            }
        }
    }
}

fun feature(build: FeatureBuilder.() -> Unit) = FeatureBuilder().apply(build).build()

class FeatureBuilder() {
    fun globalModule(create: Module.() -> Unit) = module(createdAtStart = true) {
        create()
    }

    fun scopedModule(create: ScopeDSL.() -> Unit) = module(createdAtStart = true) {
        scope<FeatureBuilder> {
            create()
        }
    }

    fun onEnable() {

    }
}

interface ModuleProvider {
    val dependsOn: List<ModuleProvider> get() = emptyList()
    fun createModule(): Module
}
