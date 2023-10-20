package com.mineinabyss.features

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.features.anticheese.AntiCheeseFeature
import com.mineinabyss.features.core.CoreFeature
import com.mineinabyss.features.cosmetics.CosmeticsFeature
import com.mineinabyss.features.curse.CurseFeature
import com.mineinabyss.features.descent.DescentFeature
import com.mineinabyss.features.displayLocker.DisplayLockerFeature
import com.mineinabyss.features.enchants.EnchantsFeature
import com.mineinabyss.features.exp.ExpFeature
import com.mineinabyss.features.gondolas.GondolaFeature
import com.mineinabyss.features.guilds.GuildFeature
import com.mineinabyss.features.hubstorage.HubStorageFeature
import com.mineinabyss.features.keepinventory.KeepInvFeature
import com.mineinabyss.features.layers.LayersFeature
import com.mineinabyss.features.misc.MiscFeature
import com.mineinabyss.features.music.MusicFeature
import com.mineinabyss.features.npc.shopkeeping.ShopKeepingFeature
import com.mineinabyss.features.okibotravel.OkiboTravelFeature
import com.mineinabyss.features.orthbanking.OrthBankingFeature
import com.mineinabyss.features.patreons.PatreonFeature
import com.mineinabyss.features.pins.PinsFeature
import com.mineinabyss.features.playerprofile.PlayerProfileFeature
import com.mineinabyss.features.pvp.adventure.AdventurePvpFeature
import com.mineinabyss.features.pvp.survival.SurvivalPvpFeature
import com.mineinabyss.features.relics.RelicsFeature
import com.mineinabyss.features.tools.ToolsFeature
import com.mineinabyss.features.tutorial.TutorialFeature
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.features.Feature
import kotlinx.serialization.Serializable

val abyssFeatures by DI.observe<AbyssFeatureConfig>()

/**
 * @param layers A list of all the layers and sections composing them to be registered.
 * @property hubSection The hub section of the abyss, a safe place for living and trading.
 * @property guilds Guild related options.
 */
@Serializable
class AbyssFeatureConfig(
    @YamlComment("Ignore following options, enable all features")
    val enableAll: Boolean = false,
    @YamlComment("Choose which features to enable with true/false")
    val antiCheese: Boolean = false,
    val configManagement: Boolean = true,
    val core: Boolean = false,
    val cosmetics: Boolean = false,
    val curse: Boolean = false,
    val descent: Boolean = false,
    val displayLocker: DisplayLockerFeature.Config = DisplayLockerFeature.Config(),
    val enchants: Boolean = false,
    val exp: Boolean = false,
    val gondolas: Boolean = false,
    val guilds: Boolean = false,
    val hubstorage: Boolean = false,
    val keepInventory: Boolean = false,
    val layers: Boolean = false,
    val misc: Boolean = false,
    val music: Boolean = false,
    val shopkeeping: Boolean = false,
    val okiboTravel: Boolean = false,
    val orthBanking: Boolean = false,
    val patreons: Boolean = false,
    val pins: Boolean = false,
    val playerProfile: Boolean = false,
    val survivalPvp: Boolean = false,
    val adventurePvp: Boolean = false,
    val relics: Boolean = false,
    val tools: Boolean = false,
    val tutorial: Boolean = false,
) {
    val features by lazy {
        buildList<Feature> {
            fun add(condition: Boolean, feature: () -> Feature) {
                if (enableAll || condition) add(feature())
            }
            add(antiCheese) { AntiCheeseFeature() }
            add(core) { CoreFeature() }
            add(cosmetics) { CosmeticsFeature() }
            add(curse) { CurseFeature() }
            add(descent) { DescentFeature() }
            add(displayLocker.enabled) { DisplayLockerFeature(displayLocker) }
            add(enchants) { EnchantsFeature() }
            add(exp) { ExpFeature() }
            add(gondolas) { GondolaFeature() }
            add(guilds) { GuildFeature() }
            add(hubstorage) { HubStorageFeature() }
            add(keepInventory) { KeepInvFeature() }
            add(layers) { LayersFeature() }
            add(misc) { MiscFeature() }
            add(music) { MusicFeature() }
            add(shopkeeping) { ShopKeepingFeature() }
            add(okiboTravel) { OkiboTravelFeature() }
            add(orthBanking) { OrthBankingFeature() }
            add(patreons) { PatreonFeature() }
            add(pins) { PinsFeature() }
            add(playerProfile) { PlayerProfileFeature() }
            add(survivalPvp) { SurvivalPvpFeature() }
            add(adventurePvp) { AdventurePvpFeature() }
            add(relics) { RelicsFeature() }
            add(tools) { ToolsFeature() }
            add(tutorial) { TutorialFeature() }
        }
    }
}
