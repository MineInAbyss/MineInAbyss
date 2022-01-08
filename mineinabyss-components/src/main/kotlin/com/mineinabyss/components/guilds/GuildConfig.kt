package com.mineinabyss.components.guilds

import kotlinx.serialization.Serializable


/**
 * @property maxLength The maximum length of a guild name.
 * @property bannedWords List for blocking certain words from guild names.
 */
@Serializable
class GuildConfig (
    val maxLength: Int = 20,
    val bannedWords: List<String> = emptyList(),
)