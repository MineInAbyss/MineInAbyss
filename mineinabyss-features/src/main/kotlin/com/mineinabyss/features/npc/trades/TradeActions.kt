package com.mineinabyss.features.npc.trades

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlScalar
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.runtime.player
import com.mineinabyss.features.dialogs.runtime.session
import com.mineinabyss.features.npc.tradeTables
import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.error
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** A trade table key, or a table written in place */
@Serializable(with = TradeTableSource.Serializer::class)
sealed interface TradeTableSource {
    fun resolve(tables: TradeTables): TradeTable?

    class Reference(val key: PrefabKey) : TradeTableSource {
        override fun resolve(tables: TradeTables) = tables[key]
        override fun toString() = key.full
    }

    class Inline(val table: TradeTable) : TradeTableSource {
        override fun resolve(tables: TradeTables) = table
        override fun toString() = "inline"
    }

    object Serializer : KSerializer<TradeTableSource> {
        override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

        override fun deserialize(decoder: Decoder): TradeTableSource {
            val node = (decoder as? YamlInput)?.node
            if (node == null || node is YamlScalar) return Reference(decoder.decodeSerializableValue(PrefabKey.serializer()))
            return Inline(decoder.decodeSerializableValue(TradeTable.serializer()))
        }

        override fun serialize(encoder: Encoder, value: TradeTableSource) = when (value) {
            is Reference -> encoder.encodeSerializableValue(PrefabKey.serializer(), value.key)
            is Inline -> encoder.encodeSerializableValue(TradeTable.serializer(), value.table)
        }
    }
}

@Serializable
@SerialName("mineinabyss:open_trades")
@JvmInline
value class OpenTradesAction(val table: TradeTableSource) : Action {
    override fun ActionGroupContext.execute() {
        val player = player ?: return
        val tables = tradeTables ?: return abyss.logger.w("Npcs feature is not loaded, cannot open trades $table")
        val tradeTable = table.resolve(tables) ?: return player.error("This trader has no trades, please report this ($table)")
        session?.close()
        tradeTable.open(player)
    }
}
