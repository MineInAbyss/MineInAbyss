package com.mineinabyss.features

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.features.core.CoreFeatureConfig
import com.mineinabyss.features.cosmetics.CosmeticsConfig
import com.mineinabyss.features.keepinventory.KeepInvFeature
import com.mineinabyss.features.orthbanking.OrthBankingFeature
import com.mineinabyss.features.patreons.PatreonFeature
import com.mineinabyss.features.playerprofile.PlayerProfileFeature
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
    val core: CoreFeatureConfig = CoreFeatureConfig(),
    val cosmetics: CosmeticsConfig = CosmeticsConfig(),
    val curse: Toggle = Toggle(),
    val customHud: Toggle = Toggle(),
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
}
