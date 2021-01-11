package com.derongan.minecraft.mineinabyss.player

import org.bukkit.entity.Player

fun Player.openHubStorage() = this.openInventory(this.enderChest)
