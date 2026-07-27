package com.mineinabyss.features.commands

import kotlinx.serialization.Serializable

@Serializable
class CommandsConfig(
    val commands: List<MessageCommand> = listOf(),
) {
    @Serializable
    class MessageCommand(
        val name: String,
        val aliases: List<String> = listOf(),
        val description: String? = null,
        val permission: String? = null,
        val messages: List<String> = listOf(),
    )
}
