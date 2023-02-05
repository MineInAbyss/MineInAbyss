package com.mineinabyss.helpers

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val hmcCosmetics: HMCCosmeticsPlugin by lazy { Bukkit.getPluginManager().getPlugin("HMCCosmetics") as HMCCosmeticsPlugin }

fun Player.getCosmeticBackpack() = this.cosmeticUser?.getCosmetic(CosmeticSlot.BACKPACK)
fun Player.equipCosmeticBackPack(backpack: String) = this.cosmeticUser?.addPlayerCosmetic(Cosmetics.getCosmetic(backpack))
fun Player.unequipCosmeticBackpack() = this.cosmeticUser?.removeCosmeticSlot(CosmeticSlot.BACKPACK)

fun Player.getCosmeticHat() = this.cosmeticUser?.getCosmetic(CosmeticSlot.HELMET)
fun Player.equipCosmeticHat(hat: String) = this.cosmeticUser?.addPlayerCosmetic(Cosmetics.getCosmetic(hat))
fun Player.unequipCosmeticHat() = this.cosmeticUser?.removeCosmeticSlot(CosmeticSlot.HELMET)

internal val Player.cosmeticUser get() = CosmeticUsers.getUser(this)
