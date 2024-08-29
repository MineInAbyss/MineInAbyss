package com.mineinabyss.features.helpers

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

val luckPerms by lazy { Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider ?: LuckPermsProvider.get() }

val OfflinePlayer.luckpermGroups
    get() = luckPerms.userManager.getUser(uniqueId)?.getNodes(NodeType.INHERITANCE)?.stream()
        ?.map { obj: InheritanceNode -> obj.groupName }?.toList() ?: emptyList()
