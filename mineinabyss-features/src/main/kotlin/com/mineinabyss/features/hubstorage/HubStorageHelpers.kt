package com.mineinabyss.features.hubstorage

import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.features.helpers.di.Features
import org.bukkit.entity.Player

fun Player.openHubStorage() = openInventory(enderChest)
fun Player.isInHub() = Features.layers.config.hubSection == location.let { WorldManager.getSectionFor(it) }
