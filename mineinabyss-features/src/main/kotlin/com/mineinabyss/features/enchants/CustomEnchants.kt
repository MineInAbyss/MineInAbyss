package com.mineinabyss.features.enchants

import com.mineinabyss.geary.datatypes.ComponentDefinition
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.component
import com.mineinabyss.idofront.messaging.logInfo
import net.kyori.adventure.text.Component
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.KClass
import net.minecraft.world.item.enchantment.Enchantment as NMSEnchantment



object SoulBound
object FrostAspect
object BirdSwatter
object JawBreaker
object BaneOfKuongatari
object Magnetism: ComponentDefinition {
    override fun onCreate(component: Entity) {
        component.set(CustomEnchants.Type(
            maxLevel = 1
        ))
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
//    val SOULBOUND = EnchantmentWrapper(
//        "soulbound",
//        "Soulbound",
//        1,
//        EnchantmentCategory.BREAKABLE,
//        net.minecraft.world.item.enchantment.Enchantment.Rarity.RARE,
//        color(150, 10, 10),
//        net.minecraft.world.entity.EquipmentSlot.MAINHAND
//    )
//    val FROST_ASPECT = EnchantmentWrapper("frostaspect", "Frost Aspect", 2, EnchantmentTarget.WEAPON, color(0, 100, 220))
//    val BIRD_SWATTER = EnchantmentWrapper("birdswatter", "Bird Swatter", 5, EnchantmentTarget.WEAPON, color(0,220,60))
//    val JAW_BREAKER = EnchantmentWrapper("jawbreaker", "Jaw Breaker", 3, EnchantmentTarget.WEAPON, color(150,20,150))
//    val BANE_OF_KUONGATARI = EnchantmentWrapper("baneofkuongatari", "Bane of Kuongatari", 1, EnchantmentTarget.WEAPON, color(0,200,80))
//    val MAGNETISM = EnchantmentWrapper("magnetism", "Magnetism", 1, EnchantmentTarget.TOOL, color(200,200,80))

    fun register() {
    }

    private fun registerEnchantment(key: String, enchantment: NMSEnchantment) {
        var registered = true
        try {
            Registry.register(BuiltInRegistries.ENCHANTMENT, key, enchantment)
        } catch (e: Exception) {
            registered = false
            e.printStackTrace()
        }
        if (registered) {
//            enchantmentList.add(enchantment)
            logInfo("Enchantment Registered")
        }
    }

    fun unfreezeRegistry() {
        val unregisteredIntrusiveHolders =
            MappedRegistry::class.java.declaredFields.filter { it.type == MutableMap::class.java }[5]
        unregisteredIntrusiveHolders.isAccessible = true
        unregisteredIntrusiveHolders[BuiltInRegistries.ENCHANTMENT] =
            IdentityHashMap<NMSEnchantment, Holder.Reference<NMSEnchantment>>()
        val frozen: Field =
            MappedRegistry::class.java.declaredFields.filter { it.type == Boolean::class.javaPrimitiveType }[0]
        frozen.isAccessible = true
        frozen[BuiltInRegistries.BLOCK] = false
    }
}
