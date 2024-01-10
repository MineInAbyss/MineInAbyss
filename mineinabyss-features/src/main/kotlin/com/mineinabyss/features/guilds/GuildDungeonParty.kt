package com.mineinabyss.features.guilds

import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.mythicDungeons
import com.mineinabyss.idofront.entities.toPlayer
import net.playavalon.mythicdungeons.api.party.IDungeonParty
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

val guildParties = mutableMapOf<UUID, IDungeonParty>()
fun Player.guildParty() = guildParties[uniqueId] ?: guildParties.values.firstOrNull { this in it.players }
fun Player.hasGuildParty() = guildParty() != null
class GuildDungeonParty(val leader: UUID) : IDungeonParty {

    private val playerUUIDs = mutableSetOf<UUID>()

    init {
        initDungeonParty(abyss.plugin)
    }

    fun updatePartyMembers() {
        playerUUIDs.removeIf { it.toPlayer() == null }
        playerUUIDs.mapNotNull { it.toPlayer() }.forEach { player ->
            mythicDungeons
        }
    }

    override fun addPlayer(player: Player) {
        playerUUIDs += player.uniqueId
    }

    override fun removePlayer(player: Player) {
        playerUUIDs -= player.uniqueId
    }

    override fun getPlayers() = playerUUIDs.mapNotNull { it.toPlayer() }.toMutableList()

    override fun getLeader() = leader.toPlayer()!!
}