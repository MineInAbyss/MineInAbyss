package com.mineinabyss.features.custom_hud

import io.lumine.mythichud.api.HudHolder
import io.lumine.mythichud.api.MythicHUD
import org.bukkit.Bukkit
import org.bukkit.entity.Player

internal val mythicHud by lazy { Bukkit.getPluginManager().getPlugin("MythicHUD") as MythicHUD }

val Player.hudHolder: HudHolder? get() = HudHolder.get(this)
