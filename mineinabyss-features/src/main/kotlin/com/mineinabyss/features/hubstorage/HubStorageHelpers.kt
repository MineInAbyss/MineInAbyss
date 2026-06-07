package com.mineinabyss.features.hubstorage

import com.mineinabyss.deeperworld.deeperWorld
import com.mineinabyss.features.abyss
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

fun Player.openHubStorage() = openInventory(enderChest)
fun Entity.isInHub() = location.isInHub()
fun Block.isinHub() = location.isInHub()
fun Location.isInHub() = abyss.layers.config.hubSection == deeperWorld.sections[this]
