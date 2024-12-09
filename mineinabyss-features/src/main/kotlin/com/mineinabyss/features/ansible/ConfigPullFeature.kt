package com.mineinabyss.features.ansible

import com.mineinabyss.features.anticheese.AntiCheeseListener
import com.mineinabyss.idofront.commands.brigadier.commands
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners

class ConfigPullFeature : Feature() {
    override fun FeatureDSL.enable() {
        plugin.commands {
            "config" {
                requires { sender.isOp }
                "copy" {
                    executes {
                        sender.info("Running keepup to copy configs and update plugins...")
                        ProcessBuilder("/scripts/keepup")
                            .inheritIO()
                            .start()
                            .onExit()
                            .thenAccept { process ->
                                when (process.exitValue()) {
                                    0 -> sender.success("Config copy complete")
                                    else -> sender.error("Config copy failed")
                                }
                            }
                    }
                }
                "pull-remote" {
                    executes {
                        sender.info("Running ansible-pull")
                        ProcessBuilder("/scripts/ansible")
                            .inheritIO()
                            .start()
                            .onExit()
                            .thenAccept { process ->
                                when (process.exitValue()) {
                                    0 -> sender.success("Ansible pull complete")
                                    else -> sender.error("Ansible pull failed")
                                }
                            }
                    }
                }
            }
        }
        mainCommand {
            "ansible" {
                "pull" {
                }
            }
        }
        plugin.listeners(AntiCheeseListener())
    }
}
