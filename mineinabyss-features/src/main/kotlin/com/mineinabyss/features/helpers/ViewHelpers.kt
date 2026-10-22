package com.mineinabyss.features.helpers

import com.mineinabyss.features.abyss
import com.mineinabyss.features.overlay.Emotes
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

// Hotbar and offhand slots of the player's own inventory menu
private val HAND_SLOTS = 36..45

// F5 cannot be blocked, at zero distance both third person views render from the eye
fun LivingEntity.zeroCameraDistance() {
    if (getAttribute(Attribute.CAMERA_DISTANCE) == null) registerAttribute(Attribute.CAMERA_DISTANCE)
    getAttribute(Attribute.CAMERA_DISTANCE)?.baseValue = 0.0
}

/** Holds [attribute] at zero until [releaseAttribute] is called with the same [key] */
fun LivingEntity.zeroAttribute(attribute: Attribute, key: NamespacedKey) {
    getAttribute(attribute)?.run {
        removeModifier(key)
        addTransientModifier(AttributeModifier(key, -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
    }
}

fun LivingEntity.releaseAttribute(attribute: Attribute, key: NamespacedKey) {
    getAttribute(attribute)?.removeModifier(key)
}

fun Player.lockCameraDistance(key: NamespacedKey) = zeroAttribute(Attribute.CAMERA_DISTANCE, key)

fun Player.releaseCameraDistance(key: NamespacedKey) = releaseAttribute(Attribute.CAMERA_DISTANCE, key)

// The first person hand still renders with a foreign camera
fun Player.hideHands() = sendTransparentSlots(HAND_SLOTS)

fun Player.viewThrough(entity: Entity) {
    toNMS().connection.send(ClientboundSetCameraPacket(entity.toNMS()))
    hideHands()
}

fun Player.resetCamera() = toNMS().run { connection.send(ClientboundSetCameraPacket(this)) }

/**
 * Stops walking, jumping and the momentum the player already has, until [unfreeze] is called with the same [key].
 * Each key holds the player on its own, so one system letting go does not free a player another still holds.
 * The modifiers are transient and not saved with the player, so a freeze cut off by a crash is gone after rejoining
 */
fun Player.freeze(key: NamespacedKey) {
    zeroAttribute(Attribute.MOVEMENT_SPEED, key)
    zeroAttribute(Attribute.JUMP_STRENGTH, key)
    velocity = velocity.setX(0).setZ(0)
}

fun Player.unfreeze(key: NamespacedKey) {
    releaseAttribute(Attribute.MOVEMENT_SPEED, key)
    releaseAttribute(Attribute.JUMP_STRENGTH, key)
}

private val EMOTE = Regex(":([a-z0-9_]+):")
// Longer than anything shown under a held title, releaseFadeTitle replaces it to fade out
private val TITLE_HOLD = 1.hours

/** A [title] of only `:<emote>:` shows that Emojy emote, anything else is MiniMessage */
private fun Player.showFadeTitle(title: String, fadeIn: Duration, stay: Duration, fadeOut: Duration) {
    val text = EMOTE.matchEntire(title)?.let { Emotes.find(it.groupValues[1]) }?.let(Emotes::component) ?: title.miniMsg()
    showTitle(Title.title(text, Component.empty(), Title.Times.times(fadeIn.toJavaDuration(), stay.toJavaDuration(), fadeOut.toJavaDuration())))
}

fun Player.holdFadeTitle(title: String, fadeIn: Duration) = showFadeTitle(title, fadeIn, TITLE_HOLD, Duration.ZERO)

fun Player.releaseFadeTitle(title: String, fadeOut: Duration) = showFadeTitle(title, Duration.ZERO, Duration.ZERO, fadeOut)

fun postEffectSteps(base: Key, steps: Int): List<Key> = (1..steps).map { Key.key(base.namespace(), "${base.value()}_$it") }

/** Shows [step] and removes the other [steps], a null [step] removes them all */
fun Player.showPostEffectStep(steps: List<Key>, step: Key?) {
    val effects = postEffects()
    steps.filter { it != step && it in effects.values() }.forEach(effects::remove)
    if (step != null && step !in effects.values()) effects.add(step)
}

/** Swaps through [steps] in order when [on], or backwards to none, spread over [duration] */
suspend fun Player.slidePostEffects(steps: List<Key>, on: Boolean, duration: Duration, wait: suspend (Duration) -> Unit) {
    val order = if (on) steps else steps.reversed().drop(1) + null
    order.forEach { step ->
        showPostEffectStep(steps, step)
        wait(duration / order.size)
    }
}

fun Player.removePostEffects(prefixes: Collection<String>) = postEffects().run {
    values().filter { key -> key.namespace() == "mineinabyss" && prefixes.any { key.value().startsWith(it) } }.forEach(::remove)
}

fun Player.sendTransparentSlots(slots: IntRange) = toNMS().run {
    val empty = CraftItemStack.asNMSCopy(TitleItem.transparentItem)
    val stateId = inventoryMenu.incrementStateId()
    slots.forEach { connection.send(ClientboundContainerSetSlotPacket(inventoryMenu.containerId, stateId, it, empty)) }
}

fun spawnStand(viewer: Player, at: Location, configure: (ArmorStand) -> Unit): ArmorStand =
    viewer.world.spawn(at, ArmorStand::class.java) { stand ->
        stand.isVisible = false
        stand.setGravity(false)
        stand.isInvulnerable = true
        stand.isPersistent = false
        stand.isVisibleByDefault = false
        configure(stand)
    }.also { viewer.showEntity(abyss, it) }
