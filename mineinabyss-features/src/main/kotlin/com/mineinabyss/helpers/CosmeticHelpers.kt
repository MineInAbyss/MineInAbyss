package com.mineinabyss.helpers

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers
import de.skyslycer.hmcwraps.IHMCWraps
import io.lumine.cosmetics.MCCosmeticsPlugin
import io.lumine.cosmetics.managers.gestures.CustomPlayerModel
import io.lumine.cosmetics.managers.gestures.QuitMethod
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val mcCosmetics: MCCosmeticsPlugin by lazy { Bukkit.getPluginManager().getPlugin("MCCosmetics") as MCCosmeticsPlugin }
val hmcCosmetics: HMCCosmeticsPlugin by lazy { Bukkit.getPluginManager().getPlugin("HMCCosmetics") as HMCCosmeticsPlugin }
val hmcWraps: IHMCWraps by lazy { Bukkit.getPluginManager().getPlugin("HMCWraps") as IHMCWraps }

fun Player.playGesture(gestureName: String) = CustomPlayerModel(player, QuitMethod.SNEAK) { }.playAnimation(gestureName)
fun Player.getCosmeticBackpack() = this.cosmeticUser?.getCosmetic(CosmeticSlot.BACKPACK)
fun Player.equipCosmeticBackPack(backpack: String) = this.cosmeticUser?.addPlayerCosmetic(Cosmetics.getCosmetic(backpack))
fun Player.unequipCosmeticBackpack() = this.cosmeticUser?.removeCosmeticSlot(CosmeticSlot.BACKPACK)

fun Player.getCosmeticHat() = this.cosmeticUser?.getCosmetic(CosmeticSlot.HELMET)
fun Player.equipCosmeticHat(hat: String) = this.cosmeticUser?.addPlayerCosmetic(Cosmetics.getCosmetic(hat))
fun Player.unequipCosmeticHat() = this.cosmeticUser?.removeCosmeticSlot(CosmeticSlot.HELMET)

internal val Player.cosmeticUser get() = CosmeticUsers.getUser(this)
