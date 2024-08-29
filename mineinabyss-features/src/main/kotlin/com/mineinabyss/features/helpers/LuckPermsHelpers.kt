package com.mineinabyss.features.helpers

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.concurrent.TimeUnit

val luckPerms by lazy { Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider ?: LuckPermsProvider.get() }

val OfflinePlayer.luckpermGroups
    get() = kotlin.runCatching {
        luckPerms.userManager.loadUser(uniqueId).get(4, TimeUnit.SECONDS)
            .getNodes(NodeType.INHERITANCE).stream().map(InheritanceNode::getGroupName).toList()
    }.getOrNull() ?: emptyList()
