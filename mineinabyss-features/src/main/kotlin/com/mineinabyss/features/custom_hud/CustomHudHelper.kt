package com.mineinabyss.features.custom_hud

import com.mineinabyss.packy.components.packyData
import io.lumine.mythichud.api.HudHolder
import io.lumine.mythichud.api.MythicHUD
import io.lumine.mythichud.api.element.layout.HudLayout
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.jvm.optionals.getOrNull

internal val mythicHud by lazy { Bukkit.getPluginManager().getPlugin("MythicHUD") as MythicHUD }
fun CustomHudFeature.customHudEnabled(player: Player) = this.customHudTemplate in player.packyData.enabledPackIds

val Player.hudHolder: HudHolder? get() = HudHolder.get(this)
