package com.mineinabyss.features.dialogs

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.dialogs.runtime.DialogSessions
import com.mineinabyss.features.dialogs.runtime.clearUnlocks
import com.mineinabyss.features.dialogs.runtime.unlocks
import com.mineinabyss.features.helpers.loaded
import com.mineinabyss.features.helpers.prefabKeyIn
import com.mineinabyss.features.helpers.reportReload
import com.mineinabyss.features.locations.LocationPoints
import com.mineinabyss.features.locations.LocationPointsFeature
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.*
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import kotlinx.coroutines.future.await
import kotlin.io.path.div

val dialogSessions: DialogSessions? get() = loaded(DialogFeature)

val DialogFeature = module("dialogs") {
    require(get<AbyssFeatureConfig>().dialogs.enabled) { "Dialogs feature is disabled" }
    val points = singleModule(LocationPointsFeature).get<LocationPoints>()
    requirePlugins("LuxDialogues")

    val dataPath = plugin.dataPath
    val config by singleConfig<DialogsConfig>("dialogs/config.yml") { default = DialogsConfig() }
    val sessions by single { DialogSessions(dataPath / "dialogs", config) }

    listeners(sessions)
    addCloseable { sessions.closeAll() }
    // Dialogs decode Geary actions and conditions, whose serializers are only complete once Geary has loaded its prefabs.
    // Actions like warp_to also need the location points they reference
    launch {
        PrefabLoading.awaitLoaded().await()
        points.loaded.await()
        sessions.reload()
    }
}.mainCommand {
    "dialogs" {
        permission = "mineinabyss.dialogs"

        "reload" {
            executes {
                val registry = get<DialogSessions>().reload()
                sender.reportReload("dialogs", registry.keys.size, registry.errors.size)
            }
        }
        "validate" {
            executes {
                val registry = get<DialogSessions>().registry
                val problems = registry.errors + registry.danglingReferences().map { "Missing dialog: $it" }
                if (problems.isEmpty()) return@executes sender.success("All ${registry.keys.size} dialogs are valid")
                problems.forEach { sender.error(it) }
            }
        }
        "open" {
            executes.asPlayer().args("dialog" to Args.prefabKeyIn { get<DialogSessions>().registry.keys }) { key ->
                if (!get<DialogSessions>().start(player, key)) player.error("Dialog $key not found")
            }
        }
        "unlocks" {
            "list" {
                executes.asPlayer {
                    player.info("Unlocked: ${player.unlocks.joinToString().ifEmpty { "nothing" }}")
                }
            }
            "clear" {
                executes.asPlayer {
                    player.clearUnlocks()
                    player.success("Cleared your unlocks")
                }
            }
        }
    }
}
