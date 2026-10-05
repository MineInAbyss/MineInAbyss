package com.mineinabyss.features.overlay

object MinecraftFont {
    // Vanilla advance widths including the 1 px gap, unlisted characters are 6 px
    private val widths = mapOf(
        ' ' to 4, '!' to 2, '"' to 5, '\'' to 3, '(' to 5, ')' to 5, '*' to 5, ',' to 2, '.' to 2, ':' to 2, ';' to 2,
        '<' to 5, '>' to 5, '@' to 7, 'I' to 4, '[' to 4, ']' to 4, '`' to 3, 'f' to 5, 'i' to 2, 'k' to 5, 'l' to 3, 't' to 4,
        '{' to 5, '|' to 2, '}' to 5, '~' to 7,
    )

    private val tag = Regex("<[^>]+>")
    private val emoteTag = Regex(":[a-z0-9_]+:")
    private val colorTag = Regex("<(#[0-9a-fA-F]{6}|[a-z_]+)>")
    private val formatting = setOf("b", "i", "u", "st", "obf", "bold", "italic", "underlined")

    fun width(text: String): Int {
        val emotes = emoteTag.findAll(text).mapNotNull { Emotes.find(it.value.trim(':')) }.toList()
        val plain = emoteTag.replace(text) { if (Emotes.find(it.value.trim(':')) != null) "" else it.value }
        // Bitmap glyphs get the same 1 px gap as the vanilla font
        return plain.codePoints().map { widths[it.toChar()] ?: 6 }.sum() + emotes.sumOf { Emotes.width(it) + 1 }
    }

    fun wrap(text: String, maxWidth: Int): List<String> {
        val lines = mutableListOf<String>()
        var line = StringBuilder()
        var lineWidth = 0
        var color: String? = null
        val open = linkedSetOf<String>()
        fun prefix() = color.orEmpty() + open.joinToString("") { "<$it>" }
        for (rawWord in text.split(' ')) {
            val pieces = if (width(tag.replace(rawWord, "")) <= maxWidth || tag.containsMatchIn(rawWord)) listOf(rawWord) else rawWord.chunked(maxOf(1, maxWidth / 6))
            for (word in pieces) {
                val w = width(tag.replace(word, "")) + if ("b" in open || "bold" in open) tag.replace(word, "").length else 0
                val spaced = if (line.isEmpty()) w else lineWidth + 4 + w
                if (line.isNotEmpty() && spaced > maxWidth) {
                    lines += line.toString()
                    line = StringBuilder(prefix()).append(word)
                    lineWidth = w
                } else {
                    if (line.isNotEmpty()) line.append(' ')
                    line.append(word)
                    lineWidth = spaced
                }
                tag.findAll(word).forEach { m ->
                    val body = m.value.trim('<', '>')
                    val closing = body.startsWith("/")
                    val name = body.trimStart('/').substringBefore(':')
                    when {
                        name == "reset" -> open.clear().also { color = null }
                        name in formatting -> if (closing) open.remove(name) else open.add(name)
                        !closing && colorTag.matches(m.value) -> color = m.value
                    }
                }
            }
        }
        lines += line.toString()
        return lines
    }
}
