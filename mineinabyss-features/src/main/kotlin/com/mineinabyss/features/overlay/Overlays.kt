package com.mineinabyss.features.overlay

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.emojy.config.Emote
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.TitleItem
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.sound.Sound
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.*
import org.joml.Vector3f
import java.util.*

private const val SHADER_CUTOFF = 3.6
private const val MAX_PITCH = 15f
private const val MIN_PLANE_DISTANCE = 0.3
private const val FIT_MARGIN = 0.1
// Degrees of head turn that span the page height
private const val PITCH_RANGE = 120f
private const val YAW_RANGE = PITCH_RANGE * (16f / 9f)
private const val CURSOR = "overlay_cursor"
private const val CURSOR_SCALE = 0.5f
// Attack, interact and arm swing all arrive for one click
private const val CLICK_DEBOUNCE_MS = 150L
private const val STALE_EFFECT_PREFIX = "overlay/"

class OverlaySounds(
    val open: String? = "minecraft:item.book.page_turn",
    val close: String? = "minecraft:item.book.put",
    val click: String? = "minecraft:ui.button.click",
    val volume: Float = 0.6f,
)

interface OverlayScreen {
    // Must sit under mineinabyss:overlay/ so a session lost on a crash gets cleaned up on join
    val postEffect: Key
    val sounds: OverlaySounds get() = OverlaySounds()
    fun build(session: Overlays.Session): List<PlaneElement>
}

object Overlays {
    class Session(
        val player: Player,
        var screen: OverlayScreen,
        val camera: ArmorStand,
        val cursorEmote: Emote,
        val cursor: TextDisplay,
        val mirrorCursor: TextDisplay,
        val view: PlaneView,
        val mirrorView: PlaneView,
        val origin: Location,
        val walkSpeed: Float,
    ) {
        var elements: List<PlaneElement> = emptyList()
        // F5 cannot be canceled, so the front view gets its own copy of the page behind the player
        var mirror: List<PlaneElement> = emptyList()
        var hovered: PlaneElement? = null
        var cx = 0.5f
        var cy = 0.5f
        var lastYaw = 0f
        var lastPitch = 0f
        var lastClick = 0L

        fun close() = Overlays.close(player)

        // A cart can only seat the player once they are off the camera stand
        fun runOutside(block: () -> Unit) {
            close()
            abyss.launch {
                delay(1.ticks)
                if (player.isOnline) block()
            }
        }

        fun refresh() {
            val built = runCatching { screen.build(this) }.getOrElse { error ->
                abyss.logger.w("Overlay ${screen.postEffect} failed to build: $error")
                close()
                return
            }
            val mirrored = runCatching { screen.build(this) }.getOrDefault(emptyList())
            (place(elements, built, view) + place(mirror, mirrored, mirrorView)).forEach(PlaneElement::remove)
            elements = built
            mirror = mirrored
            applyEffect()
            // Inventory syncs during the session bring the real hotbar back
            hideHands(player)
            hoverOver(target())
            hint()
        }

        private fun place(old: List<PlaneElement>, new: List<PlaneElement>, view: PlaneView) = old.toMutableList().also { spare ->
            new.forEach { element ->
                spare.firstOrNull { it.key == element.key && element.adopt(it, view) }?.let(spare::remove) ?: element.spawn(player, view)
            }
        }

        fun target() = elements.filter { it.clickable && it.contains(cx, cy) }.minByOrNull { it.hitBox.distanceSq(cx, cy) }

        fun hoverOver(element: PlaneElement?) {
            if (element === hovered) return
            hovered?.let { hover(it, false) }
            element?.let { hover(it, true) }
            hovered = element
            hint()
        }

        private fun hover(element: PlaneElement, on: Boolean) {
            element.hover(on)
            mirror.firstOrNull { it.key == element.key }?.hover(on)
        }

        // Re-sending the same chain makes the client rebuild it and drop a frame
        private fun applyEffect() = player.postEffects().takeUnless { it.values() == listOf(screen.postEffect) }?.run {
            clear()
            add(screen.postEffect)
        }

        fun hint() = player.sendActionBar((hovered?.label?.let { "<gold>$it" } ?: "<gray>Move the mouse to aim, click to select, sneak to close").miniMsg())
    }

    private val sessions = mutableMapOf<UUID, Session>()
    private val cameraKey = NamespacedKey("mineinabyss", "overlay/camera")

    fun session(player: Player) = sessions[player.uniqueId]

    private fun Player.sound(key: String?, sounds: OverlaySounds) = key?.let { playSound(Sound.sound(Key.key(it), Sound.Source.MASTER, sounds.volume, 1f)) }

    fun open(player: Player, screen: OverlayScreen, eye: Location? = null) {
        if (player.gameMode == GameMode.SPECTATOR) return player.error("This cannot be opened while spectating")
        sessions[player.uniqueId]?.let { it.screen = screen; return it.refresh() }
        player.sound(screen.sounds.open, screen.sounds)
        start(player, screen, eye).refresh()
    }

    fun closeAll() = sessions.keys.toList().forEach { id -> Bukkit.getPlayer(id)?.let(::close) }

    private fun fit(player: Player, eye: Location): PlaneView {
        val fit = (-2..2).flatMap { gy -> (-2..2).map { gx -> gx to gy } }.minOf { (gx, gy) ->
            val ray = PlaneLayout.worldAt(eye, 0.5 + gx * 0.4, 0.5 + gy * 0.35, SHADER_CUTOFF + FIT_MARGIN).toVector().subtract(eye.toVector())
            val length = ray.length()
            player.world.rayTraceBlocks(eye, ray.normalize(), length, FluidCollisionMode.NEVER, true)
                ?.let { it.hitPosition.distance(eye.toVector()) / length - FIT_MARGIN / SHADER_CUTOFF } ?: 1.0
        }
        return PlaneView(eye, fit.coerceAtLeast(MIN_PLANE_DISTANCE / PlaneLayout.PLANE_DISTANCE))
    }

    private fun start(player: Player, screen: OverlayScreen, fixedEye: Location?): Session {
        val cursorEmote = Emotes.get(CURSOR)
        val origin = player.location
        val yaw = fixedEye?.yaw ?: player.yaw
        val pitch = fixedEye?.pitch ?: player.pitch.coerceIn(-MAX_PITCH, MAX_PITCH)
        player.setRotation(yaw, pitch)
        val camera = player.world.spawn(fixedEye ?: player.location, ArmorStand::class.java) { stand ->
            stand.isVisible = false
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.isPersistent = false
            stand.isVisibleByDefault = false
            stand.setRotation(yaw, pitch)
            // At zero distance both F5 views render from the eye
            stand.getAttribute(Attribute.CAMERA_DISTANCE)?.baseValue = 0.0
        }
        // A stand's eyes sit higher than a player's, which puts the view inside a low ceiling
        camera.teleport(camera.location.subtract(0.0, camera.eyeHeight - (if (fixedEye != null) 0.0 else player.eyeHeight), 0.0))
        val eye = camera.eyeLocation.clone().apply { this.pitch = pitch }
        val view = if (fixedEye != null) PlaneView(eye, 1.0) else fit(player, eye)
        val mirrorView = fit(player, eye.clone().apply { this.yaw += 180f; this.pitch = -this.pitch })
        fun cursorAt(view: PlaneView) = player.world.spawn(view.at(0.5, 0.5, PlaneLayout.NEAR_DISTANCE), TextDisplay::class.java) { display ->
            display.text(Emotes.component(cursorEmote))
            display.isSeeThrough = false
            display.isShadowed = false
            display.isDefaultBackground = false
            display.backgroundColor = Color.fromARGB(0, 0, 0, 0)
            display.billboard = Display.Billboard.CENTER
            display.brightness = Display.Brightness(15, 15)
            display.teleportDuration = 1
            display.isPersistent = false
            display.isVisibleByDefault = false
            display.transformation = display.transformation.apply { scale.set(Vector3f(view.scale(CURSOR_SCALE))) }
        }
        val cursor = cursorAt(view)
        val mirrorCursor = cursorAt(mirrorView)
        listOf(camera, cursor, mirrorCursor).forEach { player.showEntity(abyss, it) }
        // A leaked session may have left the speed at zero, never snapshot that
        val session = Session(player, screen, camera, cursorEmote, cursor, mirrorCursor, view, mirrorView, origin, player.walkSpeed.takeIf { it > 0f } ?: 0.2f)
            .apply { lastYaw = yaw; lastPitch = pitch }
        // The client only reports rotation while riding its camera entity
        camera.addPassenger(player)
        sessions[player.uniqueId] = session
        player.walkSpeed = 0f
        pullCameraIn(player)
        OverlayPacketHandler.install(player)
        // The stand reaches the client on the next tracker tick, the camera packet must follow it
        abyss.launch {
            delay(2.ticks)
            if (sessions[player.uniqueId] !== session) return@launch
            player.toNMS().connection.send(ClientboundSetCameraPacket(camera.toNMS()))
            hideHands(player)
        }
        return session.also(::moveCursor)
    }

    private fun pullCameraIn(player: Player) = player.getAttribute(Attribute.CAMERA_DISTANCE)?.run {
        removeModifier(cameraKey)
        addTransientModifier(AttributeModifier(cameraKey, -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
    }

    private fun releaseCamera(player: Player) = player.getAttribute(Attribute.CAMERA_DISTANCE)?.removeModifier(cameraKey)

    fun close(player: Player) {
        val session = sessions.remove(player.uniqueId) ?: return
        OverlayPacketHandler.uninstall(player)
        player.toNMS().run { connection.send(ClientboundSetCameraPacket(this)) }
        restore(player, session)
        releaseCamera(player)
        (session.elements + session.mirror).forEach(PlaneElement::remove)
        listOf(session.camera, session.cursor, session.mirrorCursor).forEach(Entity::remove)
        player.walkSpeed = session.walkSpeed
        player.updateInventory()
        // The swallowed hotbar packets left the client on another slot
        player.inventory.heldItemSlot = player.inventory.heldItemSlot
        player.postEffects().clear()
        player.sendActionBar(Component.empty())
        player.sound(session.screen.sounds.close, session.screen.sounds)
    }

    // Vanilla drops a dismounting passenger on top of the stand, inside a low ceiling.
    // When the client dismounted itself, vanilla moves the player after the event, so this waits a tick
    private fun restore(player: Player, session: Session) {
        if (player.vehicle === session.camera) {
            if (!player.teleport(session.origin)) player.leaveVehicle()
            return
        }
        abyss.launch {
            delay(1.ticks)
            player.takeIf { it.isOnline && !it.isDead && it.world == session.origin.world && it.location.distanceSquared(session.origin) < 9.0 }
                ?.teleport(session.origin)
        }
    }

    fun look(player: Player, yaw: Float, pitch: Float) = sessions[player.uniqueId]?.run {
        cx = (cx + wrap(yaw - lastYaw) / YAW_RANGE).coerceIn(-0.3f, 1.3f)
        cy = (cy + (pitch - lastPitch) / PITCH_RANGE).coerceIn(-0.2f, 1.2f)
        lastYaw = yaw
        lastPitch = pitch
        moveCursor(this)
        hoverOver(target())
    }

    // Post effects and walk speed are saved with the player, so an overlay left open on a crash must be undone on join
    fun clearIfStale(player: Player) {
        if (player.uniqueId in sessions) return
        player.postEffects().takeIf { effects -> effects.values().any { it.namespace() == "mineinabyss" && it.value().startsWith(STALE_EFFECT_PREFIX) } }
            ?.clear()
        if (player.walkSpeed == 0f) player.walkSpeed = 0.2f
    }

    fun click(player: Player) {
        val session = sessions[player.uniqueId] ?: return
        session.lastClick = System.currentTimeMillis().takeUnless { it - session.lastClick < CLICK_DEBOUNCE_MS } ?: return
        val action = session.hovered?.onClick ?: return
        player.sound(session.screen.sounds.click, session.screen.sounds)
        action()
    }

    private fun moveCursor(session: Session) {
        val cursor = session.cursorEmote
        val tip = Emotes.bounds(cursor)
        // Drawn closer than the page, so each text pixel covers more of it
        val near = PlaneLayout.PLANE_DISTANCE / PlaneLayout.NEAR_DISTANCE
        val x = session.cx + (Emotes.width(cursor) / 2.0 - tip.left) * PlaneLayout.textWidth(1, CURSOR_SCALE) * near
        val y = session.cy + (2 + cursor.ascent - tip.top) * PlaneLayout.textHeight(1, CURSOR_SCALE) * near
        session.cursor.teleport(session.view.at(x, y, PlaneLayout.NEAR_DISTANCE))
        session.mirrorCursor.teleport(session.mirrorView.at(x, y, PlaneLayout.NEAR_DISTANCE))
    }

    // The first person hand still renders with a foreign camera and covers the cursor
    fun hideHands(player: Player) = player.toNMS().run {
        val empty = CraftItemStack.asNMSCopy(TitleItem.transparentItem)
        val stateId = inventoryMenu.incrementStateId()
        (36..45).forEach { connection.send(ClientboundContainerSetSlotPacket(inventoryMenu.containerId, stateId, it, empty)) }
    }

    private fun wrap(degrees: Float) = ((degrees + 180f) % 360f + 360f) % 360f - 180f
}
