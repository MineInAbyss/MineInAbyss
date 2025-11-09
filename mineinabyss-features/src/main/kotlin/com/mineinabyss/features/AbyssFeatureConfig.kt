package com.mineinabyss.features

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.features.core.CoreConfig
import com.mineinabyss.features.cosmetics.CosmeticsConfig
import com.mineinabyss.features.keepinventory.KeepInvConfig
import com.mineinabyss.features.orthbanking.OrthBankingConfig
import com.mineinabyss.features.patreons.PatreonConfig
import com.mineinabyss.features.playerprofile.PlayerProfileConfig
import com.mineinabyss.features.tools.ToolsFeature
import com.mineinabyss.features.tutorial.TutorialFeature
import com.mineinabyss.idofront.features.Feature
import kotlinx.serialization.Serializable

@Serializable
class Toggle(val enabled: Boolean = false)

@Serializable
class AbyssFeatureConfig(
    @YamlComment("Ignore following options, enable all features")
    val enableAll: Boolean = false,
    val ansiblePull: Toggle = Toggle(),
    @YamlComment("Choose which features to enable with true/false")
    val antiCheese: Toggle = Toggle(),
    val core: CoreConfig = CoreConfig(),
    val cosmetics: CosmeticsConfig = CosmeticsConfig(),
    val curse: Toggle = Toggle(),
    val custom_hud: Toggle = Toggle(),
    val descent: Toggle = Toggle(),
    val displayLocker: Toggle = Toggle(),
    val gondolas: Toggle = Toggle(),
    val guilds: Toggle = Toggle(),
    val hubstorage: Toggle = Toggle(),
    val keepInventory: KeepInvConfig = KeepInvConfig(),
    val layers: Toggle = Toggle(),
    val lootCrates: Toggle = Toggle(),
    val misc: Toggle = Toggle(),
    val music: Toggle = Toggle(),
    val shopkeeping: Toggle = Toggle(),
    val okiboTravel: Toggle = Toggle(),
    val orthBanking: OrthBankingConfig = OrthBankingConfig(),
    val patreon: PatreonConfig = PatreonConfig(),
    val playerProfile: PlayerProfileConfig = PlayerProfileConfig(),
    val pvp: Toggle = Toggle(),
    val relics: Toggle = Toggle(),
    val tools: Toggle = Toggle(),
    val tutorial: Toggle = Toggle(),
) {
    val features by lazy {
        buildList<Feature> {
            fun add(condition: Boolean, feature: () -> Feature) {
                if (enableAll || condition) add(feature())
            }
//            add(antiCheese.enabled) { AntiCheeseFeature() }
//            add(core.enabled) { CoreFeature(core) }
//            add(cosmetics.enabled) { CosmeticsFeature(cosmetics) }
//            add(curse.enabled) { CurseFeature() }
//            add(custom_hud.enabled) { CustomHudFeature() }
//            add(descent.enabled) { DescentFeature() }
//            add(displayLocker.enabled) { DisplayLockerFeature() }
//            add(gondolas.enabled) { GondolaFeature() }
//            add(guilds.enabled) { GuildFeature() }
//            add(hubstorage.enabled) { HubStorageFeature() }
//            add(keepInventory.enabled) { KeepInvFeature(keepInventory) }
//            add(layers.enabled) { LayersFeature() }
//            add(lootCrates.enabled) { LootCratesFeature() }
//            add(misc.enabled) { MiscFeature() }
//            add(music.enabled) { MusicFeature() }
//            add(shopkeeping.enabled) { ShopKeepingFeature() }
//            add(okiboTravel.enabled) { OkiboTravelFeature() }
//            add(orthBanking.enabled) { OrthBankingFeature(orthBanking) }
//            add(patreon.enabled) { PatreonFeature(patreon) }
//            add(playerProfile.enabled) { PlayerProfileFeature(playerProfile) }
//            add(pvp.enabled) { PvpFeature() }
//            add(relics.enabled) { RelicsFeature() }
            add(tools.enabled) { ToolsFeature }
            add(tutorial.enabled) { TutorialFeature }
//            add(ansiblePull.enabled) { ConfigPullFeature() }
//            add(true) { QuestFeature() }
        }
    }
}
