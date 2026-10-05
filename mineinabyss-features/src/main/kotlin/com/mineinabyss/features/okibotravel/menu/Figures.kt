package com.mineinabyss.features.okibotravel.menu

import com.destroystokyo.paper.ClientOption
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers
import com.mineinabyss.features.abyss
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object Figures {
    fun mannequin(viewer: Player, owner: Player, at: Location, scale: Double): Mannequin =
        viewer.world.spawn(at, Mannequin::class.java) { m ->
            m.profile = ResolvableProfile.resolvableProfile(owner.playerProfile)
            m.setSkinParts(owner.getClientOption(ClientOption.SKIN_PARTS))
            m.description = Component.empty()
            m.isCustomNameVisible = false
            m.isImmovable = true
            m.setGravity(false)
            m.isInvulnerable = true
            m.isSilent = true
            m.isCollidable = false
            m.isPersistent = false
            m.isVisibleByDefault = false
            m.getAttribute(Attribute.SCALE)?.baseValue = scale
            val gear = owner.equipment
            m.equipment.setHelmet(cosmetic(owner) { CosmeticSlot.HELMET } ?: gear.helmet)
            m.equipment.setChestplate(gear.chestplate)
            m.equipment.setLeggings(gear.leggings)
            m.equipment.setBoots(gear.boots)
            m.equipment.setItemInMainHand(gear.itemInMainHand)
            m.equipment.setItemInOffHand(gear.itemInOffHand)
            m.setRotation(at.yaw, 0f)
            m.bodyYaw = at.yaw
        }.also { viewer.showEntity(abyss, it) }

    fun markerStand(viewer: Player, at: Location, scale: Double): ArmorStand =
        viewer.world.spawn(at, ArmorStand::class.java) { stand ->
            stand.isVisible = false
            stand.isMarker = true
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.isPersistent = false
            stand.isVisibleByDefault = false
            stand.getAttribute(Attribute.SCALE)?.baseValue = scale
            stand.setRotation(at.yaw, 0f)
        }.also { viewer.showEntity(abyss, it) }

    fun backpack(owner: Player): ItemStack? = cosmetic(owner) { CosmeticSlot.BACKPACK }

    // The slot is read lazily, touching CosmeticSlot without HMCCosmetics loaded throws
    private fun cosmetic(owner: Player, slot: () -> CosmeticSlot): ItemStack? = if (!abyss.isHMCCosmeticsEnabled) null else runCatching {
        CosmeticUsers.getUser(owner)?.getCosmetic(slot())?.item
    }.getOrNull()
}
