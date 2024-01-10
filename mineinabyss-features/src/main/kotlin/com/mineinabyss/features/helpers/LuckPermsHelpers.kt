package com.mineinabyss.features.helpers

import com.mineinabyss.features.abyss
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val luckPerms by lazy { abyss.plugin.server.servicesManager.getRegistration(LuckPerms::class.java)?.provider ?: LuckPermsProvider.get() }

val Player.luckpermGroups
    get() = luckPerms.userManager.getUser(uniqueId)?.getNodes(NodeType.INHERITANCE)?.stream()
        ?.map { obj: InheritanceNode -> obj.groupName }?.toList() ?: emptyList()
