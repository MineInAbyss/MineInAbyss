package com.mineinabyss.features.npc

import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.runtime.FOCUS_KEY
import com.mineinabyss.features.dialogs.runtime.session
import com.mineinabyss.features.helpers.KeyOrInline
import com.mineinabyss.features.helpers.KeyOrInlineSerializer
import com.mineinabyss.features.helpers.ScalarShorthandSerializer
import com.mineinabyss.features.helpers.player
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.nexomc.nexo.utils.seconds
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.SerialName
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
@SerialName("mineinabyss:target_npc")
@JvmInline
value class TargetNpcAction(val npc: PrefabKey) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val spawner = npcSpawner ?: return abyss.logger.w("Npcs feature is not loaded, cannot target npc $npc")
        val definition = spawner.npcs[npc] ?: return abyss.logger.w("target_npc names unknown npc $npc")
        register(FOCUS_KEY, npc)
        val provider = LuxDialoguesAPI.getProvider()
        spawner.entity(npc)?.let { provider.focusEntity(player, it) }
            ?: provider.focusLocation(player, definition.location.clone().add(0.0, player.eyeHeight, 0.0))
        spawner.face(npc, player)
    }
}

typealias TradeTableSource = @Serializable(TradeTableSourceSerializer::class) KeyOrInline<TradeTable>

object TradeTableSourceSerializer : KeyOrInlineSerializer<TradeTable>(TradeTable::serializer)

@Serializable
@SerialName("mineinabyss:open_trades")
@JvmInline
value class OpenTradesAction(val table: TradeTableSource) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val tables = tradeTables ?: return abyss.logger.w("Npcs feature is not loaded, cannot open trades $table")
        val tradeTable = table.resolveWith(tables::get) ?: return player.error("This trader has no trades, please report this ($table)")
        session?.close()
        tradeTable.open(player)
    }
}

/** Written as just an animation name, or as a map of its fields */
@KeepGeneratedSerializer
@Serializable(PlayAnimationAction.Serializer::class)
class PlayAnimationAction(
    val animation: String,
    val npc: PrefabKey? = null,
    val speed: Double = 1.0,
    val lerpIn: @Serializable(DurationSerializer::class) Duration = Duration.ZERO,
    val lerpOut: @Serializable(DurationSerializer::class) Duration = Duration.ZERO,
    val force: Boolean = false,
) : Action {
    override fun ActionGroupContext.execute() {
        val spawner = npcSpawner ?: return abyss.logger.w("Npcs feature is not loaded, cannot play animation $animation")
        val target = npc ?: environment[FOCUS_KEY] as? PrefabKey
            ?: return abyss.logger.w("play_animation $animation has no npc and no target_npc was set")
        if (!spawner.playAnimation(target, animation, lerpIn.seconds.toDouble(), lerpOut.seconds.toDouble(), speed, force))
            abyss.logger.w("Could not play animation $animation on npc $target, it is either not loaded or has no such animation")
    }

    object Serializer : ScalarShorthandSerializer<PlayAnimationAction>(
        generatedSerializer(), { PlayAnimationAction(it.decodeString()) }, "mineinabyss:play_animation"
    )
}
