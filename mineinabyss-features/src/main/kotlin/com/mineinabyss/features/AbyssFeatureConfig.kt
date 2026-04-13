package com.mineinabyss.features

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.features.core.CoreFeatureConfig
import com.mineinabyss.features.cosmetics.CosmeticsConfig
import com.mineinabyss.features.keepinventory.KeepInvConfig
import com.mineinabyss.features.orthbanking.OrthBankConfig
import com.mineinabyss.features.patreons.PatreonConfig
import com.mineinabyss.features.playerprofile.PlayerProfileConfig
import kotlinx.serialization.Serializable

@Serializable
class Toggle(val enabled: Boolean = false)

/**
 * Root config for MineInAbyss, defines which features are enabled (each feature checks this via a `require` block when it starts.)
 *
 * Some smaller features put their config directly in here, while larger ones use separate files or multiple sets of files as needed.
 */
@Serializable
class AbyssFeatureConfig(
    @YamlComment("Ignore following options, enable all features")
    val enableAll: Boolean = false, //TODO reimplement
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
    val keepInventory: KeepInvConfig = KeepInvConfig(),
    val layers: Toggle = Toggle(),
    val lootCrates: Toggle = Toggle(),
    val misc: Toggle = Toggle(),
    val music: Toggle = Toggle(),
    val shopkeeping: Toggle = Toggle(),
    val okiboTravel: Toggle = Toggle(),
    val orthBanking: OrthBankConfig = OrthBankConfig(),
    val patreon: PatreonConfig = PatreonConfig(),
    val playerProfile: PlayerProfileConfig = PlayerProfileConfig(),
    val pvp: Toggle = Toggle(),
    val relics: Toggle = Toggle(),
    val tools: Toggle = Toggle(),
    val tutorial: Toggle = Toggle(),
)
