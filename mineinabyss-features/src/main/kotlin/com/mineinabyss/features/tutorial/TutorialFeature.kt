package com.mineinabyss.features.tutorial

import com.mineinabyss.dependencies.*
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.config.SingleConfig
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.messaging.success
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.TextDisplay

val TutorialFeature: DI.Module = module("tutorial") {
    require(get<AbyssFeatureConfig>().tutorial.enabled) { "Tutorial feature is disabled" }

    val config by singleConfig<Tutorial>("tutorial.yml")
    single<TutorialContext> {
        object : TutorialContext {
            override val firstJoinLocation: Location? = config.firstJoinLocation
            override val tutorialEntities: Long2ObjectOpenHashMap<ObjectArrayList<TutorialEntity>> =
                config.tutorialEntities.groupByTo(Long2ObjectOpenHashMap()) {
                    Chunk.getChunkKey(it.location)
                }.mapValuesTo(Long2ObjectOpenHashMap()) { (_, entities) ->
                    ObjectArrayList(entities)
                }

            override val entry: TutorialRegion = config.start
            override val exit: TutorialRegion = config.end
        }
    }

    listeners(new(::TutorialListener))

    get<TutorialContext>().spawnTutorialEntities()
    addCloseable {
        Bukkit.getWorlds().flatMap { it.entities }
            .filter { it is TextDisplay && it.toGearyOrNull()?.has<TutorialEntity>() == true }
            .forEach(Entity::remove)
    }

}.mainCommand {
    "save" {
        executes {
            val context = get<TutorialContext>()
            val entities = Bukkit.getWorlds().flatMap { it.entities.filterIsInstance<TextDisplay>() }
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
                }
            //TODO implement registering SingleConfig in idofront
            get<SingleConfig<Tutorial>>().write(
                Tutorial(context.firstJoinLocation, entities, context.entry, context.exit)
            )

            abyss.scope.reload(TutorialFeature)
            sender.success("Successfully saved tutorial-entities")
        }
    }
}
