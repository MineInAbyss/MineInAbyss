package com.mineinabyss.features.okibotravel.menu

import com.mineinabyss.components.okibotravel.MapPosition
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.features.okibotravel.OkiboRepository
import com.mineinabyss.features.okibotravel.OkiboTravelConfig
import com.mineinabyss.features.okibotravel.TravelResult
import com.mineinabyss.features.overlay.Box
import com.mineinabyss.features.overlay.Emotes
import com.mineinabyss.features.overlay.MinecraftFont
import com.mineinabyss.features.overlay.OverlayElement
import com.mineinabyss.features.overlay.OverlayScreen
import com.mineinabyss.features.overlay.Overlays
import com.mineinabyss.features.overlay.PlaneElement
import com.mineinabyss.features.overlay.PlaneLayout
import com.mineinabyss.emojy.config.Emote
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.key.Key
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Vector3f
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

private val POST_EFFECT = Key.key("mineinabyss", "overlay/okibo_board")
private const val ICON = "okibo_icon"
private const val ICON_HERE = "okibo_icon_here"
private const val ICON_SELECTED = "okibo_icon_selected"
private const val ICON_SCALE = 0.3f
private const val SELECTED_SCALE = 0.35f

private const val MAP_X = 0.35
private const val MAP_Y = 0.17
private const val MAP_W = 0.3
private const val MAP_H = 0.6
private const val STAGE_X = 0.02
private const val STAGE_W = 0.24
private const val STAGE_Y = 0.30
private const val STAGE_H = 0.62

private const val BODY_SCALE = 0.4f
private const val SMALL_SCALE = 0.35f
private const val LINE_HEIGHT_PX = 10
private const val SPACE_PX = 4
// Space between text blocks, in line heights
private const val BLOCK_GAP = 0.5
private const val PANEL_BOTTOM = 0.42
private const val PANEL_PAD = 0.016
private const val PANEL_DEPTH = PlaneLayout.PLANE_DISTANCE + 0.03
private const val BUTTON_GAP = 0.012
private val PANEL_BACKGROUND = Color.fromARGB(200, 24, 18, 13)
private val RIDE_BACKGROUND = Color.fromARGB(255, 56, 92, 44)
private val CANCEL_BACKGROUND = Color.fromARGB(255, 96, 46, 40)
private const val PANEL_PLACE = "#ffd46b"
private const val PANEL_MUTED = "#b8a785"
private const val RIDE_TEXT = "#b6e39a"
private const val CANCEL_TEXT = "#f0a08f"

class OkiboMapMenu(private val config: OkiboTravelConfig, private val repository: OkiboRepository) {
    fun open(player: Player, origin: OkiboLineStation, board: ItemDisplay, scale: Vector3f, translation: Vector3f) =
        Overlays.open(player, Screen(origin), eyeFacing(board, scale.x().toDouble(), translation))

    // An item display turns its model halfway around Y, so model -x and -z point along the entity's +x and +z
    private fun eyeFacing(board: ItemDisplay, scale: Double, t: Vector3f): Location {
        val (from, to) = config.mapFace
        val yaw = Math.toRadians(board.location.yaw.toDouble())
        val right = Vector(cos(yaw), 0.0, sin(yaw))
        val facing = Vector(-sin(yaw), 0.0, cos(yaw))
        fun local(px: Double) = (px - 8.0) / 16.0 * scale
        val center = board.location.clone()
            .add(right.clone().multiply(t.x() - local((from.x + to.x) / 2)))
            .add(0.0, t.y() + local((from.y + to.y) / 2), 0.0)
            .add(facing.clone().multiply(t.z() - local(from.z)))
        val faceHeight = (to.y - from.y) / 16.0 * scale
        val distance = PlaneLayout.PLANE_DISTANCE * faceHeight / (MAP_H * PlaneLayout.pageHeightBlocks)
        val k = distance / PlaneLayout.PLANE_DISTANCE
        val dx = (MAP_X + MAP_W / 2 - 0.5) * PlaneLayout.pageWidthBlocks * k
        val dy = (0.5 - (MAP_Y + MAP_H / 2)) * PlaneLayout.pageHeightBlocks * k
        return center
            .add(facing.clone().multiply(distance))
            .subtract(right.multiply(dx))
            .subtract(0.0, dy, 0.0)
            .setDirection(facing.multiply(-1))
    }

    private inner class Screen(val origin: OkiboLineStation) : OverlayScreen {
        var destination: OkiboLineStation? = null
        override val postEffect = POST_EFFECT

        override fun build(session: Overlays.Session): List<PlaneElement> {
            val icons = config.okiboStations.mapNotNull { station ->
                val at = station.map ?: return@mapNotNull null
                val here = station.id == origin.id
                val cost = if (here) null else repository.costBetween(origin, station)
                val label = if (here) "${station.displayName}, you are here" else listOfNotNull(station.displayName, costText(cost)).joinToString(", ")
                val (icon, scale) = when {
                    here -> ICON_HERE to ICON_SCALE
                    station == destination -> ICON_SELECTED to SELECTED_SCALE
                    else -> ICON to ICON_SCALE
                }
                icon(at, Emotes.get(icon), scale, label) {
                    destination = if (here) null else station
                    session.refresh()
                }
            }
            return icons +
                destination?.let { confirmPanel(session, it, repository.costBetween(origin, it)) }.orEmpty() +
                listOfNotNull(OkiboRiderElement.of(STAGE_X, STAGE_Y, STAGE_W, STAGE_H, config.okiboModel))
        }

        private fun confirmPanel(session: Overlays.Session, destination: OkiboLineStation, cost: Int?): List<PlaneElement> {
            val blocks = listOfNotNull(
                "<$PANEL_MUTED>Travel to" to SMALL_SCALE,
                "<$PANEL_PLACE>${destination.displayName}" to BODY_SCALE,
                costText(cost)?.let { "<$PANEL_MUTED>${it.replaceFirstChar(Char::uppercase)}" to SMALL_SCALE },
            )
            val textW = STAGE_W - 2 * PANEL_PAD
            val textH = columnHeight(blocks, textW)
            val panelTop = PANEL_BOTTOM - textH - buttonHeight - 3 * PANEL_PAD
            val buttonsTop = panelTop + textH + 2 * PANEL_PAD
            val rideW = buttonWidth("Ride")
            val left = STAGE_X + (STAGE_W - rideW - BUTTON_GAP - buttonWidth("Cancel")) / 2
            return listOf(
                backdrop(STAGE_X, panelTop, STAGE_W, PANEL_BOTTOM - panelTop),
                button(left, buttonsTop, "Ride", RIDE_TEXT, RIDE_BACKGROUND) { confirm(session, destination) },
                button(left + rideW + BUTTON_GAP, buttonsTop, "Cancel", CANCEL_TEXT, CANCEL_BACKGROUND) { this.destination = null; session.refresh() },
            ) + column(STAGE_X + PANEL_PAD, panelTop + PANEL_PAD, textW, blocks)
        }

        private fun confirm(session: Overlays.Session, destination: OkiboLineStation) {
            val player = session.player
            session.runOutside {
                when (repository.travel(player, origin, destination)) {
                    TravelResult.Success -> player.success("Enjoy your ride to <i>${destination.displayName}</i>!")
                    TravelResult.SameStation -> player.error("You are already at that station!")
                    TravelResult.NoRoute -> player.error("The okiboline is still warming up, try again in a moment!")
                    TravelResult.NotEnoughCoins -> player.error("You do not have enough coins to travel to that station!")
                    TravelResult.SpawnFailed -> player.error("Your ride could not be summoned, please let staff know!")
                }
            }
        }
    }

    private fun costText(cost: Int?) = when (cost) {
        null -> "the route is warming up"
        0 -> null
        else -> "$cost orth coins"
    }

    private fun icon(at: MapPosition, emote: Emote, scale: Float, label: String, onClick: () -> Unit): OverlayElement {
        val w = PlaneLayout.textWidth(Emotes.width(emote), scale)
        val h = PlaneLayout.textHeight(emote.height, scale)
        val x = MAP_X + at.x * MAP_W - w / 2
        val y = MAP_Y + at.y * MAP_H - h / 2
        val art = Emotes.bounds(emote)
        val hit = Box(x + PlaneLayout.textWidth(art.left, scale), y + PlaneLayout.textHeight(art.top, scale),
            PlaneLayout.textWidth(art.right - art.left, scale), PlaneLayout.textHeight(art.bottom - art.top, scale))
        return OverlayElement(x, y, w, h, Emotes.component(emote), scale,
            label = label, glyphHeight = emote.height, ascent = emote.ascent, hit = hit, onClick = onClick)
    }

    private fun wrap(blocks: List<Pair<String, Float>>, w: Double) = blocks.map { (text, scale) ->
        val width = (w * PlaneLayout.pageWidthBlocks / (PlaneLayout.TEXT_PIXEL * scale)).toInt()
        Triple(MinecraftFont.wrap(text, width), scale, width)
    }

    private fun blockHeight(lines: List<String>, scale: Float) = PlaneLayout.textHeight(LINE_HEIGHT_PX * lines.size, scale)
    private fun blockGap(scale: Float) = PlaneLayout.textHeight(LINE_HEIGHT_PX, scale) * BLOCK_GAP

    private fun columnHeight(blocks: List<Pair<String, Float>>, w: Double) = wrap(blocks, w).let { wrapped ->
        wrapped.sumOf { (lines, scale) -> blockHeight(lines, scale) } + wrapped.dropLast(1).sumOf { (_, scale) -> blockGap(scale) }
    }

    private fun column(x: Double, top: Double, w: Double, blocks: List<Pair<String, Float>>): List<OverlayElement> {
        val wrapped = wrap(blocks, w)
        val tops = wrapped.runningFold(top) { y, (lines, scale) -> y + blockHeight(lines, scale) + blockGap(scale) }
        return wrapped.zip(tops) { (lines, scale, width), y ->
            OverlayElement(x, y, w, blockHeight(lines, scale), lines.joinToString("\n").miniMsg(), scale, width + 8)
        }
    }

    private val buttonHeight = PlaneLayout.textHeight(LINE_HEIGHT_PX, BODY_SCALE)
    private fun buttonText(label: String) = "  $label  "
    private fun buttonWidth(label: String) = PlaneLayout.textWidth(MinecraftFont.width(buttonText(label)), BODY_SCALE)

    private fun button(x: Double, y: Double, label: String, color: String, background: Color, onClick: () -> Unit) =
        OverlayElement(x, y, buttonWidth(label), buttonHeight, "<$color>${buttonText(label)}".miniMsg(), BODY_SCALE,
            label = label, background = background, onClick = onClick)

    // A text display only draws its background behind text, so the panel is a block of spaces its size
    private fun backdrop(x: Double, y: Double, w: Double, h: Double): OverlayElement {
        val pixelW = w * PlaneLayout.pageWidthBlocks / (PlaneLayout.TEXT_PIXEL * BODY_SCALE)
        val pixelH = h * PlaneLayout.pageHeightBlocks / (PlaneLayout.TEXT_PIXEL * BODY_SCALE)
        val line = " ".repeat(ceil(pixelW / SPACE_PX).toInt())
        val lines = ceil(pixelH / LINE_HEIGHT_PX).toInt().coerceAtLeast(1)
        return OverlayElement(x, y, w, h, List(lines) { line }.joinToString("\n").miniMsg(), BODY_SCALE,
            lineWidth = line.length * SPACE_PX + 8, glyphHeight = lines * LINE_HEIGHT_PX, ascent = lines * LINE_HEIGHT_PX - 1,
            depth = PANEL_DEPTH, background = PANEL_BACKGROUND)
    }
}
