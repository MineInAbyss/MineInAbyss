package com.mineinabyss.features.ansible

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success

val ConfigPullFeature: Feature = feature("config-pull") {
    commands {
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
}
