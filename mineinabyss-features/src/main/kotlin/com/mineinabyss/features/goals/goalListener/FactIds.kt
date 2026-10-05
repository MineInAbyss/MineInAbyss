package com.mineinabyss.features.goals.goalListener

import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.features.goals.pairFact
import com.mineinabyss.geary.prefabs.PrefabKey
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

fun Entity.killFactIds(): List<String> = buildList {
    if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs"))  {
        MythicBukkit.inst().mobManager.getActiveMob(this@killFactIds.uniqueId).getOrNull()?.type?.internalName?.let { add("mm:$it") }
    }
    add(type.key.asString())
}

fun ItemStack.itemFactIds(world: World): List<String> = buildList {
    with(world.toGeary()) {
        persistentDataContainer.decodePrefabs().forEach { add(it.full) }
    }
    add(type.key.asString())
}

fun Entity.entityFactIds(): List<String> = killFactIds()

fun entityHitFactIds(weapon: ItemStack?, victim: Entity, world: World): List<String> {
    val weapons = weapon?.itemFactIds(world) ?: listOf(Material.AIR.key.asString())
    val targets = victim.entityFactIds()
    return weapons.flatMap { w -> targets.map { t -> pairFact(w, t) } }
}

