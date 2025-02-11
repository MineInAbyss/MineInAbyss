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
                        sender.info("Copying plugin configs...")
                        ProcessBuilder("/scripts/keepup-configs")
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
                "download-plugins" {
                    executes {
                        sender.info("Downloading plugins defined in keepup config...")
                        ProcessBuilder("/scripts/keepup-plugins")
                            .inheritIO()
                            .start()
                            .onExit()
                            .thenAccept { process ->
                                when (process.exitValue()) {
                                    0 -> sender.success("Plugin download complete")
                                    else -> sender.error("Plugin download failed")
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
