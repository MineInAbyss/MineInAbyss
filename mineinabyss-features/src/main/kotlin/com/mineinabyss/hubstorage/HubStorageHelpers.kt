package com.mineinabyss.hubstorage

import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.mineinabyss.core.abyss
import org.bukkit.entity.Player

fun Player.openHubStorage() = openInventory(enderChest)
fun Player.isInHub() = abyss.config.hubSection == location.let { WorldManager.getSectionFor(it) }
