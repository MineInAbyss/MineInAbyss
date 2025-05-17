package com.mineinabyss.features.helpers

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType
import com.hibiscusmc.hmccosmetics.user.CosmeticUser
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers
import com.mineinabyss.features.cosmetics.EmptyBackpackCosmetic
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.idofront.util.mapFast
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import me.lojosho.hibiscuscommons.util.packets.PacketManager
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

val hmcCosmetics: HMCCosmeticsPlugin by lazy { Bukkit.getPluginManager().getPlugin("HMCCosmetics") as HMCCosmeticsPlugin }

fun Player.getCosmeticBackpack() = this.cosmeticUser?.getCosmetic(CosmeticSlot.BACKPACK)
fun Player.equipCosmeticBackPack(backpack: String) = Cosmetics.getCosmetic(backpack)?.let { this.cosmeticUser?.addCosmetic(it) }
fun Player.unequipCosmeticBackpack() = this.cosmeticUser?.removeCosmeticSlot(CosmeticSlot.BACKPACK)

fun Player.getCosmeticHat() = this.cosmeticUser?.getCosmetic(CosmeticSlot.HELMET)
fun Player.equipCosmeticHat(hat: String) = Cosmetics.getCosmetic(hat)?.let { this.cosmeticUser?.addCosmetic(it) }
fun Player.unequipCosmeticHat() = this.cosmeticUser?.removeCosmeticSlot(CosmeticSlot.HELMET)

internal val Player.cosmeticUser get() = CosmeticUsers.getUser(this)

fun CosmeticUser.equipWhistleCosmetic() {
    val player = player?.takeIf { it.location.layer?.equipWhistleCosmetic == true } ?: return
    val backpack = (getCosmetic(CosmeticSlot.BACKPACK) as? CosmeticBackpackType) ?: return addCosmetic(EmptyBackpackCosmetic)
    val id = userBackpackManager?.firstArmorStandId ?: return
    val (item, itemFp) = whistleItems[player.layerIndex]

    PacketManager.equipmentSlotUpdate(id, EquipmentSlot.HAND, item, player.trackedBy.toMutableList())

    if (backpack.isFirstPersonCompadible) {
        PacketManager.equipmentSlotUpdate(id, EquipmentSlot.HAND, itemFp, mutableListOf(player))
    }
}

private val Player.layerIndex get() = when (location.layer?.id) {
    "orth" -> 0
    "layerone" -> 1
    "layertwo" -> 2
    "layerthree" -> 3
    "layerfour" -> 4
    "layerfive" -> 5
    else -> 6
}

private val whistleItems by lazy {
    val whistleItem = ItemStack.of(Material.PAPER).apply {
        setData(DataComponentTypes.ITEM_MODEL, Key.key("cosmetics", "whistle"))
    }

    (0 until Features.layers.worldManager.layers.size).mapFast {
        whistleItem.clone().apply {
            setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(it.toFloat()).addFlag(false))
        } to whistleItem.clone().apply {
            setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(it.toFloat()).addFlag(true))
        }
    }
}
