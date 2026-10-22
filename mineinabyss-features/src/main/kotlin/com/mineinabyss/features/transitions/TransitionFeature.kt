package com.mineinabyss.features.transitions

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.helpers.loaded
import com.mineinabyss.features.helpers.loading.KeyedRegistry
import com.mineinabyss.features.helpers.prefabKeyIn
import com.mineinabyss.features.helpers.reportReload
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.*
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import kotlinx.coroutines.future.await
import org.bukkit.entity.Player
import kotlin.io.path.div

typealias TransitionRegistry = KeyedRegistry<Transition>

val transitions: Transitions? get() = loaded(TransitionFeature)

fun Player.isInTransitionDark() = transitions?.isDark(this) == true

val TransitionFeature = module("transitions") {
    require(get<AbyssFeatureConfig>().transitions.enabled) { "Transitions feature is disabled" }

    val transitionsPath = plugin.dataPath / "transitions"
    val registry by single<TransitionRegistry> { KeyedRegistry(transitionsPath, "transition", "transitions", Transition.serializer()) }
    val transitions by single { Transitions() }

    listeners(transitions)
    addCloseable { transitions.cancelAll() }

    launch {
        PrefabLoading.awaitLoaded().await()
        registry.reload()
    }
}.mainCommand {
    "transitions" {
        permission = "mineinabyss.transitions"

        "reload" {
            executes {
                val errors = get<TransitionRegistry>().reload()
                sender.reportReload("transitions", get<TransitionRegistry>().entries.size, errors.size)
            }
        }
        "list" {
            executes {
                get<TransitionRegistry>().entries.forEach { (key, transition) ->
                    sender.info("$key <gray>${transition.fadeIn} in, ${transition.travelTime} dark, ${transition.fadeOut} out")
                }
            }
        }
        "play" {
            description = "Plays a transition on yourself without doing anything while the screen is dark"
            executes.asPlayer().args("transition" to Args.prefabKeyIn { get<TransitionRegistry>().entries.keys }) { key ->
                val transition = get<TransitionRegistry>().entries.getValue(key)
                if (!get<Transitions>().play(player, transition) { true }) player.error("Could not play $key")
            }
        }
    }
}
