package com.mineinabyss.features.hubstorage

import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.features.helpers.di.Features
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

fun Player.openHubStorage() = openInventory(enderChest)
fun Entity.isInHub() = location.isInHub()
fun Block.isinHub() = location.isInHub()
fun Location.isInHub() = Features.layers.config.hubSection == WorldManager.getSectionFor(this)
