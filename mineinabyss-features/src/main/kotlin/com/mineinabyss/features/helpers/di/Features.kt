package com.mineinabyss.features.helpers.di

import com.mineinabyss.features.displayLocker.DisplayLockerFeature
import com.mineinabyss.features.guidebook.GuideBookFeature
import com.mineinabyss.features.guilds.GuildFeature
import com.mineinabyss.features.layers.LayersContext
import com.mineinabyss.features.lootcrates.LootCratesFeature
import com.mineinabyss.features.music.MusicContext
import com.mineinabyss.features.okibotravel.OkiboTravelFeature
import com.mineinabyss.idofront.di.DI

object Features {
    val layers: LayersContext by DI.observe()
    val okiboLine: OkiboTravelFeature.Context by DI.observe()
    val music: MusicContext by DI.observe()
    val guilds: GuildFeature.Context by DI.observe()
    val lootCrates: LootCratesFeature.Context by DI.observe()
    val guideBook: GuideBookFeature.Context by DI.observe()
}
