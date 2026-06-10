package com.mineinabyss.features.achievements

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.achievements.data.AchievementProgress
import com.mineinabyss.features.achievements.data.AchievementStore
import com.mineinabyss.idofront.Idofront
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.datastore.launchWrite
import com.mineinabyss.idofront.datastore.readBlocking
import com.mineinabyss.idofront.datastore.setupDataStore
import com.mineinabyss.idofront.features.mainCommand

val AchievementsFeature = module("achievements") {
    require(get<AbyssFeatureConfig>().achievements.enabled) { "Achievements feature is disabled" }
    Idofront.setupDataStore(AchievementStore)
}.mainCommand {
    "achievement" {
        "unlock" {
            executes.asPlayer().args("name" to Args.string()) { name ->
                player.launchWrite { AchievementStore[player, name] = AchievementProgress(completed = true) }
            }
        }
        "status" {
            executes.asPlayer().args("name" to Args.string()) { name ->
                val progress = player.readBlocking { AchievementStore[player, name] }
                player.sendMessage("Achievement progress: $progress")
            }
        }
    }
}
