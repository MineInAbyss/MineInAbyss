package com.mineinabyss.features.curse

import com.mineinabyss.components.playerData
import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.commands.arguments.booleanArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException


class CurseFeature : Feature() {
    override val dependsOn = setOf("DeeperWorld", "WorldGuard")

    override fun FeatureDSL.enable() {
        plugin.listeners(CurseAscensionListener(), CurseEffectsListener())

        val registry = WorldGuard.getInstance().flagRegistry
        try {
            val flag = StateFlag("mine-in-abyss-curse", true)
            registry.register(flag)
            CurseFeature.ABYSS_CURSE_FLAG = flag // only set our field if there was no error
        } catch (e: FlagConflictException) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            val existing = registry["mine-in-abyss-curse"]
            if (existing is StateFlag) {
                CurseFeature.ABYSS_CURSE_FLAG = existing
            } else {
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
                abyss.logger.s("Flag conflict for mine-in-abyss-curse")
            }
        }


        mainCommand {
            "curse"(desc = "Commands to toggle curse") {
                permission = "mineinabyss.curse"

                val toggled by booleanArg()

                playerAction {
                    player.playerData.isAffectedByCurse = toggled
                    val enabled = if (toggled) "enabled" else "disabled"
                    sender.success("Curse $enabled for ${player.name}")
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf(
                    "curse"
                ).filter { it.startsWith(args[0]) }

                2 -> {
                    when (args[0]) {
                        "curse" -> listOf("on", "off")
                        else -> null
                    }
                }

                else -> null
            }
        }
    }

    // Curse def
    companion object {
        var ABYSS_CURSE_FLAG: StateFlag? = null
    }

}
