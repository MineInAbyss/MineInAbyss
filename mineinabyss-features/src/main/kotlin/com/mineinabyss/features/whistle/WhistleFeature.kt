package com.mineinabyss.features.whistle

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.whistle.dataStore.WhistleStore
import com.mineinabyss.features.whistle.repository.WhistleCache
import com.mineinabyss.features.whistle.repository.WhistleRepository
import com.mineinabyss.idofront.Idofront
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.suggests
import com.mineinabyss.idofront.datastore.setupDataStore
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.success

val whistleFeature = module("whistle")  {
    require(get<AbyssFeatureConfig>().whistle.enabled) { "Whistle feature is disabled" }
    single<WhistleConfig> { WhistleConfig() }
    single { WhistleCache() }
    single { WhistleRepository(get(), get(), WhistleStore) }
    Idofront.setupDataStore(WhistleStore)
    listeners(new(::WhistleListener))
}.mainCommand {

    "whistle" {
        "set" {
            val whistleArgs = {Args.string().suggests { suggestFiltering(get<WhistleConfig>().whistles.keys.toList()) }}
            executes.asPlayer().args("whistle" to whistleArgs()) { whistle :String ->
                val config = get<WhistleConfig>()
                val selectedWhistle = config.whistles[whistle]
                    ?: error("Unknown whistle: $whistle")

                get<WhistleRepository>().setWhistle(player, whistle)
                player.success("Your whistle is now  set to ${selectedWhistle.name}")
            }
        }
        "query" {
            executes.asPlayer() {
                player.success("Your whistle is: ${get<WhistleRepository>().whistleName(player)}")
            }
        }
    }
}
