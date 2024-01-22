package com.mineinabyss.features.enchants

import com.mineinabyss.geary.datatypes.ComponentDefinition
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.component
import org.bukkit.enchantments.Enchantment
import kotlin.reflect.KClass


object SoulBound
object FrostAspect
object BirdSwatter
object JawBreaker
object BaneOfKuongatari

object Magnetism : ComponentDefinition {
    override fun onCreate(component: Entity) {
        component.set(
            CustomEnchants.Type(
                maxLevel = 1
            )
        )
    }
}

object CustomEnchants {
    inline fun <reified T : Any> get(entity: GearyEntity): Data? {
        return entity.getRelation<Data, T>()
    }

    fun <T : Any> remove(type: KClass<T>, entity: GearyEntity) {
        entity.removeRelation<Data>(component(type))
    }

    fun <T : Any> set(type: KClass<T>, data: Data, entity: GearyEntity) {
        entity.setRelation<Data>(data, component(type))
    }

    class Data(
        val level: Int = 1,
    )

    class Type(
        val minLevel: Int = 1,
        val maxLevel: Int,
    )

    val enchantmentList = mutableListOf<Enchantment>()
}
