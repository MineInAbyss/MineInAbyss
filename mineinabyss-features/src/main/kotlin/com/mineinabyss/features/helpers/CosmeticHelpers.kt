package com.mineinabyss.features.helpers

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType
import com.hibiscusmc.hmccosmetics.user.CosmeticUser
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import me.lojosho.hibiscuscommons.util.packets.PacketManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

val hmcCosmetics: HMCCosmeticsPlugin by lazy { Bukkit.getPluginManager().getPlugin("HMCCosmetics") as HMCCosmeticsPlugin }

fun Player.getCosmeticBackpack() = this.cosmeticUser?.getCosmetic(CosmeticSlot.BACKPACK)
fun Player.equipCosmeticBackPack(backpack: String) = this.cosmeticUser?.addPlayerCosmetic(Cosmetics.getCosmetic(backpack))
fun Player.unequipCosmeticBackpack() = this.cosmeticUser?.removeCosmeticSlot(CosmeticSlot.BACKPACK)

fun Player.getCosmeticHat() = this.cosmeticUser?.getCosmetic(CosmeticSlot.HELMET)
fun Player.equipCosmeticHat(hat: String) = this.cosmeticUser?.addPlayerCosmetic(Cosmetics.getCosmetic(hat))
fun Player.unequipCosmeticHat() = this.cosmeticUser?.removeCosmeticSlot(CosmeticSlot.HELMET)

internal val Player.cosmeticUser get() = CosmeticUsers.getUser(this)

fun CosmeticUser.equipWhistleCosmetic() {
    if (!hasCosmeticInSlot(CosmeticSlot.BACKPACK)) addPlayerCosmetic(Cosmetics.getCosmetic("empty_backpack"))
    val player = player ?: return
    val layerWhistle = player.layerWhistleCosmetic() ?: return
    val viewers = player.world.getNearbyPlayers(player.location, 16.0).toMutableList()
    if ((getCosmetic(CosmeticSlot.BACKPACK) as CosmeticBackpackType).isFirstPersonCompadible) {
        viewers.remove(player)
        PacketManager.equipmentSlotUpdate(userBackpackManager.firstArmorStandId, EquipmentSlot.OFF_HAND, layerWhistle, mutableListOf(player))
    }
    PacketManager.equipmentSlotUpdate(userBackpackManager.firstArmorStandId, EquipmentSlot.HAND, layerWhistle, viewers)
}

fun Player.layerWhistleCosmetic(): ItemStack? {
    val whistle = when (simpleLayerName) {
        "orth" -> "bell"
        "edge_of_the_abyss" -> "red_whistle"
        "forest_of_temptation" -> "blue_whistle"
        "great_fault", "the_goblets_of_giants" -> "moon_whistle"
        "sea_of_corpses" -> "black_whistle"
        else -> return null
    }
    val prefab = PrefabKey.ofOrNull("cosmetics:$whistle") ?: return null
    return gearyItems.itemProvider.serializePrefabToItemStack(prefab).takeIf { it != ItemStack.empty() }
}
