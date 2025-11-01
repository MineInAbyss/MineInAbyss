package com.mineinabyss.features

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.features.ansible.ConfigPullFeature
import com.mineinabyss.features.anticheese.AntiCheeseFeature
import com.mineinabyss.features.core.CoreFeature
import com.mineinabyss.features.cosmetics.CosmeticsFeature
import com.mineinabyss.features.curse.CurseFeature
import com.mineinabyss.features.custom_hud.CustomHudFeature
import com.mineinabyss.features.descent.DescentFeature
import com.mineinabyss.features.displayLocker.DisplayLockerFeature
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
import com.mineinabyss.features.playerprofile.PlayerProfileFeature
import com.mineinabyss.features.pvp.PvpFeature
import com.mineinabyss.features.quests.QuestFeature
import com.mineinabyss.features.relics.RelicsFeature
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
    val core: CoreFeature.Config = CoreFeature.Config(),
    val cosmetics: CosmeticsFeature.Config = CosmeticsFeature.Config(),
    val curse: Toggle = Toggle(),
    val custom_hud: Toggle = Toggle(),
    val descent: Toggle = Toggle(),
    val displayLocker: Toggle = Toggle(),
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
    val playerProfile: PlayerProfileFeature.Config = PlayerProfileFeature.Config(),
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
            add(antiCheese.enabled) { AntiCheeseFeature() }
            add(core.enabled) { CoreFeature(core) }
            add(cosmetics.enabled) { CosmeticsFeature(cosmetics) }
            add(curse.enabled) { CurseFeature() }
            add(custom_hud.enabled) { CustomHudFeature() }
            add(descent.enabled) { DescentFeature() }
            add(displayLocker.enabled) { DisplayLockerFeature() }
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
            add(playerProfile.enabled) { PlayerProfileFeature(playerProfile) }
            add(pvp.enabled) { PvpFeature() }
            add(relics.enabled) { RelicsFeature() }
            add(tools.enabled) { ToolsFeature() }
            add(tutorial.enabled) { TutorialFeature() }
            add(ansiblePull.enabled) { ConfigPullFeature() }
            add(true) { QuestFeature() }
        }
    }
}
