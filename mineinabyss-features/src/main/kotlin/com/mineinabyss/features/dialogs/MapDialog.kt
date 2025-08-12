package com.mineinabyss.features.dialogs

import com.mineinabyss.emojy.config.Emote
import com.mineinabyss.emojy.emojy
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import org.bukkit.entity.Player

open class MapDialog(
    val habo: Boolean,
    val okibo: Boolean,
    val pogjaw: Boolean
) {

    fun showDialog(player: Player) {
        val dialog = Dialog.create { builder ->
            builder.empty().base(
                DialogBase.builder(Component.text("<shift:360><glyph:logo><shift:400>")).body(
                    listOf(
                        DialogBody.plainMessage(mapComponent, 1024)
                    ).plus(DialogBody.plainMessage(selectionComponent, 1024))
                ).pause(false).canCloseWithEscape(true).afterAction(DialogBase.DialogAfterAction.NONE).build()
            ).type(DialogType.notice(ActionButton.builder(Component.empty()).action(null).build()))
        }

        player.showDialog(dialog)
    }

    val mapComponent: Component by lazy {
        val mapEmote = emojy.emotes.find { it.id == "map" } ?: return@lazy Component.text("Missing Map Emote")

        mapEmote.unicodes.foldIndexed(Component.empty()) { index, initial, line ->
            initial.append(mapWithMarkers(index, line) ?: Component.empty()).appendNewline()
        }.color(NamedTextColor.WHITE).shadowColor(ShadowColor.none()).font(Key.key("nexo:default"))
    }

    val haboGlyph by lazy { emojy.emotes.find { it.id == "map" }!! }
    val okiboGlyph by lazy { emojy.emotes.find { it.id == "okibo" }!! }
    val pogjawGlyph by lazy { emojy.emotes.find { it.id == "pogjaw" }!! }
    private fun mapWithMarkers(rowIndex: Int, line: String): Component? {
        return when (rowIndex) {
            14 if okibo -> markerComponent(
                listOf(
                    Marker(14, okiboGlyph, -12, "Golden Bridge Station"),
                    Marker(12, okiboGlyph, 3, "GuildHQ Station")
                ),
                line
            )
            16 if okibo -> markerComponent(10, line, okiboGlyph, -12, "Orphanage Station")
            20 if pogjaw -> markerComponent(20, line, pogjawGlyph, -12, "test")
            0 if pogjaw -> markerComponent(0, line, pogjawGlyph, -24, "Test")
            else -> line.joinToString(Space.Companion.of(-1)).let { Component.text(it) }
        }
    }

    data class Marker(
        val columnIndex: Int,
        val emote: Emote,
        val shift: Int = 0,
        val hover: String? = null
    )

    fun markerComponent(markers: List<Marker>, line: String): Component {
        if (markers.isEmpty()) {
            return line.joinToString(Space.Companion.of(-1)).let { Component.text(it) }
        }

        val sortedMarkers = markers.sortedBy { it.columnIndex }
        val components = mutableListOf<Component>()

        var lastIndex = 0
        for (marker in sortedMarkers) {
            val beforeSplit = line.substring(lastIndex, marker.columnIndex).joinToString(Space.Companion.of(-1))
            val markerComponent = marker.emote.formattedComponent().hoverEvent(marker.hover?.miniMsg()?.let { HoverEvent.showText(it) })
            val shiftComponent = if (marker.shift == 0) Component.empty() else Component.text(Space.Companion.of(marker.shift))

            if (beforeSplit.isNotEmpty()) {
                components.add(Component.text(beforeSplit))
            }
            components.add(shiftComponent)
            components.add(markerComponent)

            lastIndex = marker.columnIndex
        }

        val afterSplit = line.substring(lastIndex).joinToString(Space.Companion.of(-1))
        if (afterSplit.isNotEmpty()) {
            components.add(Component.text(afterSplit))
        }

        return Component.textOfChildren(*components.toTypedArray())
            .shadowColor(ShadowColor.none()).color(NamedTextColor.WHITE)
    }

    fun markerComponent(columnIndex: Int, line: String, emote: Emote, shift: Int = 0, hover: String? = null): Component {
        return markerComponent(listOf(Marker(columnIndex, emote, shift, hover)), line)
    }

    val selectionComponent: Component = Component.textOfChildren(
        Component.text("  habo ${if (habo) "☑" else "☐"}").color(NamedTextColor.WHITE).font(Key.key("default"))
            .clickEvent(ClickEvent.custom(DialogHelpers.dialogId, BinaryTagHolder.binaryTagHolder("{habo:${!habo},okibo:${okibo},pogjaw:${pogjaw}}"))),
        Component.text("  okibo ${if (okibo) "☑" else "☐"}").color(NamedTextColor.WHITE).font(Key.key("default"))
            .clickEvent(ClickEvent.custom(DialogHelpers.dialogId, BinaryTagHolder.binaryTagHolder("{habo:${habo},okibo:${!okibo},pogjaw:${pogjaw}}"))),
        Component.text("  pogjaw ${if (pogjaw) "☑" else "☐"}").color(NamedTextColor.WHITE).font(Key.key("default"))
            .clickEvent(ClickEvent.custom(DialogHelpers.dialogId, BinaryTagHolder.binaryTagHolder("{habo:${habo},okibo:${okibo},pogjaw:${!pogjaw}}"))),
    )
}