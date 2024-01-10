package com.mineinabyss.features.helpers

import com.mineinabyss.features.abyss
import net.playavalon.mythicdungeons.MythicDungeons
import net.playavalon.mythicdungeons.api.MythicDungeonsService
import org.bukkit.Bukkit

class MythicDungeonsHelpers {
}

val mythicDungeons by lazy { (abyss.plugin.server.servicesManager.load(MythicDungeonsService::class.java) ?: MythicDungeons.inst())!! }