package com.mineinabyss.fasttravel.okiboline

import com.mineinabyss.hubstorage.isInHub
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("okibo_line")
class OkiboLineFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(OkiboLineListener())
        commands {
            mineinabyss {
                "okibo_line"(desc = "Commands related to Okibo Line System in Orth") {
                    playerAction {
                        if (!player.isInHub()) {
                            player.error("You must be in <gold>Orth</gold> to use this command.")
                            return@playerAction
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("okibo_line").filter { it.startsWith(args[0]) }
                    else -> null
                }
            }
        }
    }
}
