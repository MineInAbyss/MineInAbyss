package com.mineinabyss.guilds.extensions

import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.guilds.GuildFeature
import com.mineinabyss.guilds.database.*
import com.mineinabyss.helpers.MessageQueue
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.looty.LootyFactory
import com.mineinabyss.looty.ecs.components.itemcontexts.PlayerInventorySlotContext
import com.mineinabyss.looty.ecs.components.itemcontexts.useWithLooty
import com.mineinabyss.looty.loadItem
import com.mineinabyss.looty.tracking.toGearyOrNull
import com.mineinabyss.mineinabyss.core.AbyssContext
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun OfflinePlayer.createGuild(guildName: String, feature: GuildFeature) {
    val newGuildName = guildName.replace("\\s".toRegex(), "") // replace space to avoid: exam ple

    if (newGuildName.length > feature.guildNameMaxLength) {
        player?.error("Your guild name was longer than the maximum allowed length.")
        player?.error("Please make it shorter than ${feature.guildNameMaxLength} characters.")
        return
    }

    feature.guildNameBannedWords.forEach { banned ->
        val bannedWord = banned.toRegex().find(newGuildName)?.value
        if (banned.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(newGuildName)) {
            if (bannedWord?.contains("([^a-zA-Z])".toRegex()) == true)
                player?.error("Your Guild name can only contain the letters <b>a-z</b>.")
            else
                player?.error("Your Guild name contains a blocked word: <b>$bannedWord</b>.")
            player?.error("Please choose another name :)")
            return
        }
    }

    transaction(AbyssContext.db) {

        val guild = Guilds.select {
            Guilds.name.lowerCase() eq guildName.lowercase()
        }.firstOrNull()

        if (guild != null) {
            player?.error("There is already a guild registered with the name <i>$guildName</i>!")
            return@transaction
        } else player?.success("Your Guild has been registered with the name <i>$guildName")

        val rowID = Guilds.insert {
            it[name] = guildName
            it[balance] = 0
            it[level] = 1
            it[joinType] = GuildJoinType.Any
        } get Guilds.id

        Players.insert {
            it[playerUUID] = uniqueId
            it[guildId] = rowID
            it[guildRank] = GuildRanks.Owner
        }
    }
}

fun Player.deleteGuild() {
    transaction(AbyssContext.db) {
        /* Find the owners guild */
        val guildId = Players.select {
            Players.playerUUID eq uniqueId
        }.firstOrNull()?.get(Players.guildId) ?: return@transaction

        if (getGuildRank() != GuildRanks.Owner) {
            this@deleteGuild.error("Only the Owner can disband the guild.")
            return@transaction
        }

        /* Message to all guild-members */
        Players.select {
            (Players.guildId eq guildId) and (Players.playerUUID neq uniqueId)
        }.forEach { row ->
            val deleteGuildMessage = "The Guild you were in has been deleted by the Owner."
            val player = Bukkit.getPlayer(row[Players.playerUUID])

            player?.error(deleteGuildMessage) ?: MessageQueue.insert {
                it[content] = deleteGuildMessage
                it[playerUUID] = row[Players.playerUUID]
            }
        }

        /* Give guildbalance to owner or one who deleted guild */
        val owner = Bukkit.getOfflinePlayer(getGuildOwner())
        if (owner.isOnline) (owner as Player).playerData.orthCoinsHeld += getGuildBalance()
        else playerData.orthCoinsHeld += getGuildBalance()

        /* Delete join-requests & invites if the guild is deleted */
        GuildJoinQueue.deleteWhere {
            GuildJoinQueue.guildId eq guildId
        }

        /* Remove guild entry from Guilds db thus removing all members */
        Guilds.deleteWhere {
            Guilds.id eq guildId
        }

        Players.deleteWhere {
            Players.guildId eq guildId
        }

        /* Message to owner */
        success("Your Guild has been deleted!")
    }
}

fun Player.changeStoredGuildName(newGuildName: String) {
    transaction(AbyssContext.db) {

        val guild = Guilds.select {
            Guilds.name.lowerCase() eq newGuildName.lowercase()
        }.firstOrNull()

        val guildId = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]



        if (guild != null) {
            error("There is already a guild registered with this name!")
            return@transaction
        }

        Guilds.update({ Guilds.id eq guildId }) {
            it[name] = newGuildName
        }

        val guildName = getGuildName()
        val changedNameMessage =
            "<yellow>The Guild you are in has been renamed to <gold><i>$guildName!"
        /* Message to all guild-members */
        Players.select {
            (Players.guildId eq guildId) and (Players.playerUUID neq uniqueId)
        }.forEach { row ->

            val player = Bukkit.getPlayer(row[Players.playerUUID])
            if (player != null) {
                player.info(changedNameMessage)
            } else {
                MessageQueue.insert {
                    it[content] = changedNameMessage
                    it[playerUUID] = row[Players.playerUUID]
                }
            }
        }
        success("Your guild was successfully renamed to <gold><i>$guildName!")
    }
}

fun Player.changeGuildJoinType() {
    transaction(AbyssContext.db) {
        val guildId = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        val type = Guilds.select {
            Guilds.id eq guildId
        }.single()[Guilds.joinType]

        val newType = when (type) {
            GuildJoinType.Any -> GuildJoinType.Request
            GuildJoinType.Invite -> GuildJoinType.Any
            GuildJoinType.Request -> GuildJoinType.Invite
        }

        Guilds.update({ Guilds.id eq guildId }) {
            it[joinType] = newType
        }
    }
}

fun Player.getGuildMembers(): List<Pair<GuildRanks, OfflinePlayer>> {
    return transaction(AbyssContext.db) {
        val playerRow = Players.select {
            Players.playerUUID eq player!!.uniqueId
        }.single()

        val guildId = playerRow[Players.guildId]

        Players.select {
            (Players.guildId eq guildId)
        }.map { row ->
            Pair(row[Players.guildRank], Bukkit.getOfflinePlayer(row[Players.playerUUID]))
        }
    }
}

fun String.getGuildMembers(): List<Pair<GuildRanks, OfflinePlayer>> {
    return transaction(AbyssContext.db) {
        val guild = Guilds.select {
            Guilds.name.lowerCase() eq this@getGuildMembers.lowercase()
        }.singleOrNull()?.get(Guilds.id) ?: return@transaction emptyList<Pair<GuildRanks, OfflinePlayer>>()

        Players.select {
            (Players.guildId eq guild)
        }.map { row ->
            Pair(row[Players.guildRank], Bukkit.getOfflinePlayer(row[Players.playerUUID]))
        }
    }
}

fun getAllGuilds(): List<Triple<String, GuildJoinType, Int>> {
    return transaction(AbyssContext.db) {
        return@transaction Guilds.selectAll()
            .map { row -> Triple(row[Guilds.name], row[Guilds.joinType], row[Guilds.level]) }
    }
}

fun displayGuildList(queryName: String? = null): List<Triple<String, GuildJoinType, Int>> {
    val guilds = getAllGuilds()
    val comparator = compareBy<Triple<String, GuildJoinType, Int>> { it.third; it.first.getOwnerFromGuildName().getGuildMemberCount(); it.second; it.first }
    return if (queryName == null)
        guilds.sortedWith(comparator)
    else guilds.filter { it.first.startsWith(queryName) }.sortedWith(comparator)
}

fun String.getOwnerFromGuildName(): OfflinePlayer {
    return transaction(AbyssContext.db) {
        val guild = Guilds.select {
            Guilds.name eq this@getOwnerFromGuildName
        }.first()[Guilds.id]

        val player = Players.select {
            Players.guildId eq guild
        }.first()[Players.playerUUID]

        return@transaction Bukkit.getOfflinePlayer(player)
    }
}

fun Player.depositCoinsToGuild(amount: Int) {
    if (!hasGuild()) {
        error("You must be in a guild to withdraw coins.")
        return
    }

    if (inventory.itemInMainHand.toGearyOrNull(this)?.has<OrthCoin>() != true) {
        error("You must be holding an Orth Coin to make a deposit.")
        return
    }

    inventory.itemInMainHand.subtract(amount)
    updateGuildBalance(amount)
    success("You deposited $amount Orth Coins to your guild.")
}

fun Player.withdrawCoinsFromGuild(amount: Int) {
    if (!hasGuild()) {
        error("You must be in a guild to withdraw coins.")
        return
    }

    if (!isGuildOwner()) {
        error("You must be the guild owner to withdraw coins.")
        return
    }

    if (getGuildBalance() - amount < 0) {
        error("You do not have enough coins in your guild to withdraw $amount coins.")
        return
    }

    val slot = inventory.firstEmpty()
    if (slot == -1) {
        error("You do not have enough space in your inventory to withdraw the coins.")
        return
    }

    val orthCoin = LootyFactory.createFromPrefab(PrefabKey.Companion.of("mineinabyss:orthcoin"))
    orthCoin?.useWithLooty {
        PlayerInventorySlotContext(this@withdrawCoinsFromGuild, slot).loadItem(this)
    }
    if (orthCoin == null) {
        error("Could not create OrthCoin.")
        error("Cancelling withdrawal!")
        return
    }

    updateGuildBalance(-amount)
    inventory.addItem(orthCoin.asQuantity(amount))
    success("You withdrew $amount Orth Coins from your guild.")
}

fun Player.canLevelUpGuild(): Boolean {
    val guild = getGuildName()
    val levelUpCost = guild.getGuildLevelUpCost() ?: return false
    val balance = getGuildBalance()

    return balance >= levelUpCost
}

fun String.canLevelUpGuild(): Boolean {
    val levelUpCost = getGuildLevelUpCost() ?: return false
    val balance = getGuildBalance()

    return balance >= levelUpCost
}

fun Player.levelUpGuild() {
    val guildName = getGuildName()
    val cost = guildName.getGuildLevelUpCost() ?: return
    val guildMembers = getGuildMembers().filter { it.second.uniqueId != uniqueId }.map { it.second }
    updateGuildBalance(-cost)
    transaction(AbyssContext.db) {
        val lvl = Guilds.select {
            Guilds.name.lowerCase() eq guildName.lowercase()
        }.singleOrNull()?.get(Guilds.level) ?: return@transaction 0

        Guilds.update({ Guilds.name.lowerCase() eq guildName.lowercase() }) {
            it[level] = lvl + 1
        }
    }

    val newLvl = guildName.getGuildLevel()
    val lvlUpMessage = "<gold>Your guild has leveled up to level <b>${newLvl}</b>!"

    success("You have leveled up your guild to level <b>${newLvl}</b>!")
    guildMembers.forEach { member ->
        if (member.isOnline) {
            (member as Player).sendMessage(lvlUpMessage)
        } else {
            MessageQueue.insert {
                it[content] = lvlUpMessage
                it[playerUUID] = member.uniqueId
            }
        }
    }
    closeInventory()
}

fun String.getGuildLevelUpCost(): Int? {
    return when (getGuildLevel()) {
        1 -> 25
        2 -> 50
        3 -> 100
        4 -> 200
        else -> null
    }
}

private fun Player.updateGuildBalance(amount: Int) {
    transaction(AbyssContext.db) {
        val guildId = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        val bal = Guilds.select {
            Guilds.id eq guildId
        }.single()[Guilds.balance]

        Guilds.update({ Guilds.id eq guildId }) {
            it[balance] = (bal + amount)
        }
    }
}

private fun String.updateGuildBalance(amount: Int) {
    transaction(AbyssContext.db) {
        val bal = Guilds.select {
            Guilds.name eq this@updateGuildBalance
        }.single()[Guilds.balance]

        Guilds.update({ Guilds.name eq this@updateGuildBalance }) {
            it[balance] = (bal + amount)
        }
    }
}
