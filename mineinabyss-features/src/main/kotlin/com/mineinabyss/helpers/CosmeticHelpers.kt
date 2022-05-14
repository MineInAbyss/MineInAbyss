package com.mineinabyss.helpers

import com.mineinabyss.components.cosmetics.cosmetics
import com.mineinabyss.mineinabyss.core.mcCosmetics
import io.github.fisher2911.hmccosmetics.api.CosmeticItem
import io.github.fisher2911.hmccosmetics.api.HMCCosmeticsAPI
import io.github.fisher2911.hmccosmetics.gui.ArmorItem
import io.lumine.cosmetics.managers.gestures.GestureManager
import io.lumine.cosmetics.players.Profile
import org.bukkit.entity.Player

fun Player.playGesture(gesture: String) {
    mcCosmetics.profiles.getProfile(name).ifPresent { profile: Profile? ->
        mcCosmetics.gestureManager.getCosmetic(gesture).ifPresent { gesture ->
            cosmetics.gesture = gesture
            gesture.equip(profile)
            (gesture.manager as GestureManager).playGesture(profile)
        }
    }
}

fun Player.getCosmeticHat(): CosmeticItem = HMCCosmeticsAPI.getUserCurrentItem(uniqueId, ArmorItem.Type.HAT)
fun Player.equipCosmeticHat(hat: String) =
    HMCCosmeticsAPI.setCosmeticItem(uniqueId, HMCCosmeticsAPI.getCosmeticFromId(hat))
fun Player.unequipCosmeticHat() =
    HMCCosmeticsAPI.setCosmeticItem(uniqueId, CosmeticItem(ArmorItem.empty(ArmorItem.Type.HAT)))

fun Player.getCosmeticBackpack(): CosmeticItem = HMCCosmeticsAPI.getUserCurrentItem(uniqueId, ArmorItem.Type.BACKPACK)
fun Player.equipCosmeticBackPack(backpack: String) =
    HMCCosmeticsAPI.setCosmeticItem(uniqueId, HMCCosmeticsAPI.getCosmeticFromId(backpack))
fun Player.unequipCosmeticBackpack() =
    HMCCosmeticsAPI.setCosmeticItem(uniqueId, CosmeticItem(ArmorItem.empty(ArmorItem.Type.BACKPACK)))
