package com.mineinabyss.helpers

import com.mineinabyss.components.cosmetics.Cosmetics
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import io.github.fisher2911.hmccosmetics.HMCCosmetics
import io.github.fisher2911.hmccosmetics.api.CosmeticItem
import io.github.fisher2911.hmccosmetics.api.HMCCosmeticsAPI
import io.github.fisher2911.hmccosmetics.gui.ArmorItem
import io.lumine.cosmetics.MCCosmeticsPlugin
import io.lumine.cosmetics.managers.gestures.CustomPlayerModel
import io.lumine.cosmetics.managers.gestures.QuitMethod
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val mcCosmetics: MCCosmeticsPlugin by lazy { Bukkit.getPluginManager().getPlugin("MCCosmetics") as MCCosmeticsPlugin }
val hmcCosmetics: HMCCosmetics by lazy { Bukkit.getPluginManager().getPlugin("HMCCosmetics") as HMCCosmetics }

fun Player.playGesture(gestureName: String) {
    toGeary().setPersisting(Cosmetics(gesture = gestureName))
    CustomPlayerModel(player, QuitMethod.SNEAK) { }.playAnimation(gestureName)
}

fun Player.getCosmeticBackpack(): CosmeticItem =
    HMCCosmeticsAPI.getUserCurrentItem(uniqueId, ArmorItem.Type.BACKPACK)
fun Player.equipCosmeticBackPack(backpack: String) =
    HMCCosmeticsAPI.setCosmeticItem(uniqueId, HMCCosmeticsAPI.getCosmeticFromId(backpack))
fun Player.unequipCosmeticBackpack() =
    HMCCosmeticsAPI.setCosmeticItem(uniqueId, CosmeticItem(ArmorItem.empty(ArmorItem.Type.BACKPACK)))

fun Player.getCosmeticHat(): CosmeticItem =
    HMCCosmeticsAPI.getUserCurrentItem(uniqueId, ArmorItem.Type.HAT)
fun Player.equipCosmeticHat(hat: String) =
    HMCCosmeticsAPI.setCosmeticItem(uniqueId, HMCCosmeticsAPI.getCosmeticFromId(hat))
fun Player.unequipCosmeticHat() =
    HMCCosmeticsAPI.setCosmeticItem(uniqueId, CosmeticItem(ArmorItem.empty(ArmorItem.Type.HAT)))
