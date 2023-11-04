package com.mineinabyss.features

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.features.advancements.AdvancementsFeature
import com.mineinabyss.features.anticheese.AntiCheeseFeature
import com.mineinabyss.features.core.CoreFeature
import com.mineinabyss.features.cosmetics.CosmeticsFeature
import com.mineinabyss.features.curse.CurseFeature
import com.mineinabyss.features.custom_hud.CustomHudFeature
import com.mineinabyss.features.descent.DescentFeature
import com.mineinabyss.features.displayLocker.DisplayLockerFeature
import com.mineinabyss.features.enchants.EnchantsFeature
import com.mineinabyss.features.exp.ExpFeature
import com.mineinabyss.features.gondolas.GondolaFeature
import com.mineinabyss.features.guilds.GuildFeature
import com.mineinabyss.features.hubstorage.HubStorageFeature
import com.mineinabyss.features.keepinventory.KeepInvFeature
import com.mineinabyss.features.layers.LayersFeature
import com.mineinabyss.features.lootcrates.LootCratesFeature
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

@Serializable
class Toggle(val enabled: Boolean = false)

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
    val advancement: Toggle = Toggle(),
    val antiCheese: Toggle = Toggle(),
    val configManagement: Toggle = Toggle(),
    val core: Toggle = Toggle(),
    val cosmetics: CosmeticsFeature.Config = CosmeticsFeature.Config(),
    val curse: Toggle = Toggle(),
    val custom_hud: Toggle = Toggle(),
    val descent: Toggle = Toggle(),
    val displayLocker: DisplayLockerFeature.Config = DisplayLockerFeature.Config(),
    val enchants: Toggle = Toggle(),
    val exp: Toggle = Toggle(),
    val gondolas: Toggle = Toggle(),
    val guilds: Toggle = Toggle(),
    val hubstorage: Toggle = Toggle(),
    val keepInventory: KeepInvFeature.Config = KeepInvFeature.Config(),
    val layers: Toggle = Toggle(),
    val lootCrates: Toggle = Toggle(),
    val misc: Toggle = Toggle(),
    val music: Toggle = Toggle(),
    val shopkeeping: Toggle = Toggle(),
    val okiboTravel: Toggle = Toggle(),
    val orthBanking: OrthBankingFeature.Config = OrthBankingFeature.Config(),
    val patreon: PatreonFeature.Config = PatreonFeature.Config(),
    val pins: Toggle = Toggle(),
    val playerProfile: Toggle = Toggle(),
    val survivalPvp: Toggle = Toggle(),
    val adventurePvp: Toggle = Toggle(),
    val relics: Toggle = Toggle(),
    val tools: Toggle = Toggle(),
    val tutorial: Toggle = Toggle(),
) {
    val features by lazy {
        buildList<Feature> {
            fun add(condition: Boolean, feature: () -> Feature) {
                if (enableAll || condition) add(feature())
            }
            add(advancement.enabled) { AdvancementsFeature() }
            add(antiCheese.enabled) { AntiCheeseFeature() }
            add(core.enabled) { CoreFeature() }
            add(cosmetics.enabled) { CosmeticsFeature(cosmetics) }
            add(curse.enabled) { CurseFeature() }
            add(custom_hud.enabled) { CustomHudFeature() }
            add(descent.enabled) { DescentFeature() }
            add(displayLocker.enabled) { DisplayLockerFeature(displayLocker) }
            add(enchants.enabled) { EnchantsFeature() }
            add(exp.enabled) { ExpFeature() }
            add(gondolas.enabled) { GondolaFeature() }
            add(guilds.enabled) { GuildFeature() }
            add(hubstorage.enabled) { HubStorageFeature() }
            add(keepInventory.enabled) { KeepInvFeature(keepInventory) }
            add(layers.enabled) { LayersFeature() }
            add(lootCrates.enabled) { LootCratesFeature() }
            add(misc.enabled) { MiscFeature() }
            add(music.enabled) { MusicFeature() }
            add(shopkeeping.enabled) { ShopKeepingFeature() }
            add(okiboTravel.enabled) { OkiboTravelFeature() }
            add(orthBanking.enabled) { OrthBankingFeature(orthBanking) }
            add(patreon.enabled) { PatreonFeature(patreon) }
            add(pins.enabled) { PinsFeature() }
            add(playerProfile.enabled) { PlayerProfileFeature() }
            add(survivalPvp.enabled) { SurvivalPvpFeature() }
            add(adventurePvp.enabled) { AdventurePvpFeature() }
            add(relics.enabled) { RelicsFeature() }
            add(tools.enabled) { ToolsFeature() }
            add(tutorial.enabled) { TutorialFeature() }
        }
    }
}
