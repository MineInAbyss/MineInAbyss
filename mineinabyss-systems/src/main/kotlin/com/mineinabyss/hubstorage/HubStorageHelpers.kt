package com.mineinabyss.hubstorage

import org.bukkit.entity.Player

fun Player.openHubStorage() = openInventory(enderChest)
