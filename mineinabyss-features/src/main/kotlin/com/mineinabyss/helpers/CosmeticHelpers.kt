package com.mineinabyss.helpers

import com.mineinabyss.components.cosmetics.Cosmetics
import com.mineinabyss.geary.papermc.access.toGeary
import io.lumine.cosmetics.MCCosmeticsPlugin
import io.lumine.cosmetics.api.cosmetics.ItemCosmetic
import io.lumine.cosmetics.managers.back.BackAccessory
import io.lumine.cosmetics.managers.gestures.CustomPlayerModel
import io.lumine.cosmetics.managers.gestures.QuitMethod
import io.lumine.cosmetics.managers.hats.Hat
import io.lumine.cosmetics.managers.offhand.Offhand
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val mcCosmetics: MCCosmeticsPlugin by lazy { Bukkit.getPluginManager().getPlugin("MCCosmetics") as MCCosmeticsPlugin }

fun Player.playGesture(gestureName: String) {
    toGeary { setPersisting(Cosmetics(gesture = gestureName)) }
    CustomPlayerModel(player, QuitMethod.SNEAK) { }.playAnimation(gestureName)
}

fun Player.getCosmeticHat() : ItemStack {
    val profile = mcCosmetics.profiles.getProfile(this).getEquipped(Hat::class.java) ?: return ItemStack(Material.AIR)
    val hat = profile.get().cosmetic as? ItemCosmetic ?: return ItemStack(Material.AIR)
    return ItemStack(hat.getCosmetic(profile.get().variant))
}
fun Player.equipCosmeticHat(hat: String) = mcCosmetics.profiles.getProfile(this).equip(mcCosmetics.hatManager.getCosmetic(hat).get())
fun Player.unEquipCosmeticHat() = mcCosmetics.profiles.getProfile(this).unequip(Hat::class.java)

fun Player.getCosmeticBackpack() : ItemStack {
    val profile = mcCosmetics.profiles.getProfile(this).getEquipped(BackAccessory::class.java) ?: return ItemStack(Material.AIR)
    val backpack = profile.get().cosmetic as? ItemCosmetic ?: return ItemStack(Material.AIR)
    return ItemStack(backpack.getCosmetic(profile.get().variant))
}
fun Player.equipCosmeticBackpack(backpack: String) = mcCosmetics.profiles.getProfile(this).equip(mcCosmetics.hatManager.getCosmetic(backpack).get())
fun Player.unEquipCosmeticBackpack() = mcCosmetics.profiles.getProfile(this).unequip(BackAccessory::class.java)

fun Player.getCosmeticOffhand() : ItemStack {
    val profile = mcCosmetics.profiles.getProfile(this).getEquipped(Offhand::class.java) ?: return ItemStack(Material.AIR)
    val offhand = profile.get().cosmetic as? ItemCosmetic ?: return ItemStack(Material.AIR)
    return ItemStack(offhand.getCosmetic(profile.get().variant))
}
fun Player.equipCosmeticOffhand(offhand: String) = mcCosmetics.profiles.getProfile(this).equip(mcCosmetics.hatManager.getCosmetic(offhand).get())
fun Player.unEquipCosmeticOffhand() = mcCosmetics.profiles.getProfile(this).unequip(Offhand::class.java)
