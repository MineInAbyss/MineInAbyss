package com.mineinabyss.features.goals

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class FactKind {
    REGION_ENTER,
    CRAFT,
    KILL,
    CLIMB,
    PICKUP,
    CRAFT_ANY,
    COMPLETE,
    CATEGORY_PICKUP,
    WEAPON_SHOOT,
    PROJECTILE_SHOOT,
    WEAPON_HIT,
    PROJECTILE_HIT,
    ENTITY_HIT,
}

const val FACT_PAIR_SEPARATOR = "|"

fun pairFact(weapon: String, target: String) = "$weapon$FACT_PAIR_SEPARATOR$target"

@Serializable
data class ConditionProgress(
    val values: Set<String> = emptySet(),
    val count: Int = 0,
)

@Serializable
sealed interface GoalCondition {
    fun tracks(kind: FactKind, value: String): Boolean

    fun advance(current: ConditionProgress, value: String, amount: Int): ConditionProgress

    fun isMet(progress: ConditionProgress): Boolean

    fun describe(progress: ConditionProgress): String
}

@Serializable
@SerialName("visit")
data class VisitCondition(
    val regions: List<String>,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.REGION_ENTER && value in regions
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(values = current.values + value)
    override fun isMet(progress: ConditionProgress) = progress.values.containsAll(regions)
    override fun describe(progress: ConditionProgress) =
        "Visited ${progress.values.size}/${regions.size}: ${regions.joinToString { if (it in progress.values) "✔$it" else it }}"
}

@Serializable
@SerialName("craft")
data class CraftCondition(
    val items: List<String>,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.CRAFT && value in items
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(values = current.values + value)
    override fun isMet(progress: ConditionProgress) = progress.values.containsAll(items)
    override fun describe(progress: ConditionProgress) =
        "Crafted ${progress.values.size}/${items.size}: ${items.joinToString { if (it in progress.values) "✔$it" else it }}"
}

@Serializable
@SerialName("kill")
data class KillCondition(
    val mob: String,
    val amount: Int = 1,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.KILL && value == mob
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(count = current.count + 1)
    override fun isMet(progress: ConditionProgress) = progress.count >= amount
    override fun describe(progress: ConditionProgress) =
        "Killed ${progress.count.coerceAtMost(amount)}/$amount $mob"
}

@Serializable
@SerialName("climb")
data class ClimbCondition(val count: Int = 1): GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.CLIMB
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(count = current.count + 1)
    override fun isMet(progress: ConditionProgress) = progress.count >=  count
    override fun describe(progress: ConditionProgress): String  = "Climbed ${progress.count.coerceAtMost(count)}/$count times"

}

@Serializable
@SerialName("pickup")
data class PickupCondition(
    val item: String,
    // amount > 1 is weird, don't use it yet
    val amount: Int = 1,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.PICKUP && value == item
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(count = current.count + amount)
    override fun isMet(progress: ConditionProgress) = progress.count >= amount
    override fun describe(progress: ConditionProgress) =
        "Picked up ${progress.count.coerceAtMost(amount)}/$amount $item"
}

@Serializable
@SerialName("craft_any")
data class CraftAnyCondition(
    val items: List<String>,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.CRAFT_ANY && value in items
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(values = current.values + value)
    override fun isMet(progress: ConditionProgress) = items.any { progress.values.contains(it) }
    override fun describe(progress: ConditionProgress) =
        "Crafted ${progress.values.size}/${items.size}: ${items.joinToString { if (it in progress.values) "✔$it" else it }}"
}

@Serializable
@SerialName("category_pickup")
data class CategoryPickupCondition(
    val category: String,
    // amount > 1 is weird, don't use it yet
    val amount: Int = 1,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.CATEGORY_PICKUP && value == category
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(count = current.count + amount)
    override fun isMet(progress: ConditionProgress) = progress.count >= amount
    override fun describe(progress: ConditionProgress) =
        "Picked up ${progress.count.coerceAtMost(amount)}/$amount $category"
}

@Serializable
@SerialName("complete")
data class CompleteCondition(
    val goals: List<String>,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.COMPLETE && value in goals
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(values = current.values + value)
    override fun isMet(progress: ConditionProgress) = progress.values.containsAll(goals)
    override fun describe(progress: ConditionProgress) =
        "Completed ${progress.values.size}/${goals.size}: ${goals.joinToString { if (it in progress.values) "✔$it" else it }}"
}

@Serializable
@SerialName("weapon_shoot")
data class WeaponShootCondition(
    val weapons: List<String>,
    val amount: Int = 1,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.WEAPON_SHOOT && value in weapons
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(values = current.values + value, count = current.count + 1)
    override fun isMet(progress: ConditionProgress) = progress.count >= amount
    override fun describe(progress: ConditionProgress) =
        "Shot ${progress.count.coerceAtMost(amount)}/$amount times with ${weapons.joinToString()}"
}

@Serializable
@SerialName("projectile_shoot")
data class ProjectileShootCondition(
    val projectiles: List<String>,
    val amount: Int = 1,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.PROJECTILE_SHOOT && value in projectiles
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(values = current.values + value, count = current.count + 1)
    override fun isMet(progress: ConditionProgress) = progress.count >= amount
    override fun describe(progress: ConditionProgress) =
        "Shot ${progress.count.coerceAtMost(amount)}/$amount of ${projectiles.joinToString()}"
}

@Serializable
@SerialName("weapon_hit")
data class WeaponHitCondition(
    val weapons: List<String>,
    val amount: Int = 1,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.WEAPON_HIT && value in weapons
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(values = current.values + value, count = current.count + 1)
    override fun isMet(progress: ConditionProgress) = progress.count >= amount
    override fun describe(progress: ConditionProgress) =
        "Hit ${progress.count.coerceAtMost(amount)}/$amount entities with ${weapons.joinToString()}"
}

@Serializable
@SerialName("entity_hit")
data class EntityHitCondition(
    val weapons: List<String> = emptyList(),
    val targets: List<String> = emptyList(),
    val amount: Int = 1,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String): Boolean {
        if (kind != FactKind.ENTITY_HIT) return false
        val weapon = value.substringBefore(FACT_PAIR_SEPARATOR)
        val target = value.substringAfter(FACT_PAIR_SEPARATOR)
        return (weapons.isEmpty() || weapon in weapons) && (targets.isEmpty() || target in targets)
    }

    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(values = current.values + value, count = current.count + 1)
    override fun isMet(progress: ConditionProgress) = progress.count >= amount
    override fun describe(progress: ConditionProgress) =
        "Hit ${progress.count.coerceAtMost(amount)}/$amount ${targets.ifEmpty { listOf("entities") }.joinToString()}" +
            if (weapons.isEmpty()) "" else " with ${weapons.joinToString()}"
}

@Serializable
@SerialName("projectile_hit")
data class ProjectileHitCondition(
    val projectiles: List<String>,
    val amount: Int = 1,
) : GoalCondition {
    override fun tracks(kind: FactKind, value: String) = kind == FactKind.PROJECTILE_HIT && value in projectiles
    override fun advance(current: ConditionProgress, value: String, amount: Int) = current.copy(values = current.values + value, count = current.count + 1)
    override fun isMet(progress: ConditionProgress) = progress.count >= amount
    override fun describe(progress: ConditionProgress) =
        "Hit ${progress.count.coerceAtMost(amount)}/$amount entities with ${projectiles.joinToString()}"
}