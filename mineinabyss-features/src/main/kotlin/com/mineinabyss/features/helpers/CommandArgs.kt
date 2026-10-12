package com.mineinabyss.features.helpers

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.map
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn
import org.bukkit.command.CommandSender

internal fun Args.prefabKey() = ArgsMinecraft.key()
    .map { PrefabKey.of(it.namespace(), it.value()) }

internal fun CommandSender.reportReload(kind: String, count: Int, errors: Int) {
    if (errors == 0) success("Reloaded $count $kind")
    else warn("Reloaded $count $kind with $errors errors, see console")
}
