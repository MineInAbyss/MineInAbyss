package com.mineinabyss.features.helpers

import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.entity.Player

val luckPerms = LuckPermsProvider.get()

val Player.luckpermGroups
    get() = luckPerms.userManager.getUser(uniqueId)?.getNodes(NodeType.INHERITANCE)?.stream()
        ?.map { obj: InheritanceNode -> obj.groupName }?.toList() ?: emptyList()
