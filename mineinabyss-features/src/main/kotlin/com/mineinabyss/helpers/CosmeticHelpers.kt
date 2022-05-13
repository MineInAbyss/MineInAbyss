package com.mineinabyss.helpers

import com.mineinabyss.components.cosmetics.cosmetics
import com.mineinabyss.components.playerData
import com.mineinabyss.mineinabyss.core.mcCosmetics
import io.lumine.cosmetics.api.cosmetics.ItemCosmetic
import io.lumine.cosmetics.managers.back.BackAccessory
import io.lumine.cosmetics.managers.gestures.GestureManager
import io.lumine.cosmetics.managers.hats.Hat
import io.lumine.cosmetics.players.Profile
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun Player.playGesture(gesture: String) {
    mcCosmetics.profiles.getProfile(name).ifPresent { profile: Profile? ->
        mcCosmetics.gestureManager.getCosmetic(gesture).ifPresent { gesture ->
            cosmetics.gesture = gesture
            gesture.equip(profile)
            playerData
            (gesture.manager as GestureManager).playGesture(profile)
        }
    }
}

fun Player.getCosmeticBackpack() : ItemStack {
    val profile = mcCosmetics.profiles.getProfile(this).getEquipped(BackAccessory::class.java)
    if (profile.isEmpty) return ItemStack(Material.AIR)
    val cosmetic = profile.get().cosmetic ?: return ItemStack(Material.AIR)
    val back = cosmetic as? ItemCosmetic ?: return ItemStack(Material.AIR)
    return ItemStack(back.getCosmetic(profile.get().variant))

}

fun Player.getCosmeticHat() : ItemStack {
    val profile = mcCosmetics.profiles.getProfile(this).getEquipped(Hat::class.java)
    if (profile.isEmpty) return ItemStack(Material.AIR)
    val cosmetic = profile.get().cosmetic ?: return ItemStack(Material.AIR)
    val hat = cosmetic as? ItemCosmetic ?: return ItemStack(Material.AIR)
    return ItemStack(hat.getCosmetic(profile.get().variant))
}