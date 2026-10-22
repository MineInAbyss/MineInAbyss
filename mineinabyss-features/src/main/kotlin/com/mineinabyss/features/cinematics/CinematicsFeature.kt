package com.mineinabyss.features.cinematics

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.helpers.loaded
import com.mineinabyss.features.helpers.loading.KeyedRegistry
import com.mineinabyss.features.helpers.prefabKeyIn
import com.mineinabyss.features.helpers.reportReload
import com.mineinabyss.features.locations.LocationPoints
import com.mineinabyss.features.locations.LocationPointsFeature
import com.mineinabyss.features.npc.npcSpawner
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.*
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.coroutines.future.await
import org.bukkit.entity.Player
import java.util.*
import kotlin.io.path.div

internal class CinematicEditor {
    private val anchors = mutableMapOf<UUID, CinematicAnchor>()

    operator fun get(player: Player) = anchors[player.uniqueId]
    operator fun set(player: Player, anchor: CinematicAnchor) {
        anchors[player.uniqueId] = anchor
    }
}

internal typealias CinematicRegistry = KeyedRegistry<CinematicScene>

val cinematics: Cinematics? get() = loaded(CinematicsFeature)

val CinematicsFeature = module("cinematics") {
    require(get<AbyssFeatureConfig>().cinematics.enabled) { "Cinematics feature is disabled" }
    val points = singleModule(LocationPointsFeature).get<LocationPoints>()

    val cinematicsPath = plugin.dataPath / "cinematics"
    val registry by single<CinematicRegistry> { KeyedRegistry(cinematicsPath, "cinematic", "cinematics", CinematicScene.serializer()) }
    val cinematics by single { Cinematics(registry) }
    single { CinematicEditor() }

    listeners(cinematics)
    addCloseable { cinematics.stopAll() }

    launch {
        PrefabLoading.awaitLoaded().await()
        points.loaded.await()
        registry.reload()
    }
}.mainCommand {
    "cinematics" {
        permission = "mineinabyss.cinematics"

        "reload" {
            executes {
                val errors = get<CinematicRegistry>().reload()
                sender.reportReload("cinematics", get<CinematicRegistry>().entries.size, errors.size)
            }
        }
        "list" {
            executes {
                get<CinematicRegistry>().entries.forEach { (key, scene) -> sender.info("$key <gray>${scene.steps.size} steps") }
            }
        }
        "play" {
            description = "Plays a scene from your anchor, or from where you stand if none is set"
            executes.asPlayer().args("scene" to Args.prefabKeyIn { get<CinematicRegistry>().entries.keys }) { key ->
                val anchor = get<CinematicEditor>()[player] ?: CinematicAnchor(player.location)
                if (!get<Cinematics>().play(player, key, anchor)) player.error("Could not play $key")
            }
        }
        "stop" {
            executes.asPlayer {
                get<Cinematics>().stop(player)
            }
        }
        "anchor" {
            description = "Anchors to the closest NPC, or to the block you stand on facing the side you look at"
            executes.asPlayer {
                val npc = npcSpawner?.nearest(player.location)
                val anchor = npc?.let { CinematicAnchor(it.value.location) } ?: CinematicAnchor.snapped(player.location)
                get<CinematicEditor>()[player] = anchor
                val origin = anchor.origin
                player.success("Anchored to ${npc?.key ?: "here"} at ${origin.blockX} ${origin.blockY} ${origin.blockZ}, facing ${origin.yaw}")
            }
        }
        "pos" {
            description = "Shows where you stand and where your eyes are, relative to your anchor, click to copy"
            executes.asPlayer {
                val anchor = get<CinematicEditor>()[player] ?: return@asPlayer player.error("Set an anchor first with /mia cinematics anchor")
                fun line(label: String, position: ScenePosition) {
                    val text = "[${position.x}, ${position.y}, ${position.z}, ${position.yaw}, ${position.pitch}]"
                    player.sendMessage("<gray>$label: <click:copy_to_clipboard:'$text'><hover:show_text:'Click to copy'><white>$text".miniMsg())
                }
                line("Feet (mannequin, walk, gondola)", anchor.relative(player.location))
                line("Eyes (camera)", anchor.relative(player.eyeLocation))
            }
        }
    }
}
