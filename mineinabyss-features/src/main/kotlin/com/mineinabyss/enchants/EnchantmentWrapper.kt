package com.mineinabyss.enchants

import io.papermc.paper.enchantments.EnchantmentRarity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.EntityCategory
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

abstract class CustomEnchantment(namespace: String) : Enchantment(NamespacedKey.minecraft(namespace))

class EnchantmentWrapper(
    private val namespace: String,
    private val name: String,
    private val maxLvl: Int,
    val allowedItems: List<CustomEnchantTargets>,
    private val conflictingEnchants: List<Enchantment>,
    private val rarity: EnchantmentRarity = EnchantmentRarity.COMMON,
    val loreColor: TextColor = color(150, 10, 10)
) : CustomEnchantment(namespace) {

    override fun canEnchantItem(item: ItemStack): Boolean {
        return true
    }

    override fun getName(): String {
        return name
    }

    override fun displayName(level: Int): Component {
        val component = Component.text(name).color(loreColor).decoration(TextDecoration.ITALIC, false)

        if (level != maxLvl) {
            component.append(Component.text(" ${convertEnchantmentLevel(level)}"))
        }

        return component
    }

    override fun translationKey(): String {
        return "enchantment.$namespace"
    }

    override fun getStartLevel(): Int {
        return 1
    }

    override fun getMaxLevel(): Int {
        return maxLvl
    }

    override fun conflictsWith(other: Enchantment): Boolean {
        return conflictingEnchants.contains(other)
    }

    override fun getRarity(): EnchantmentRarity {
        return rarity
    }

    fun getCustomItemTarget(): List<CustomEnchantTargets> {
        return allowedItems
    }

    fun getCustomEnchantmentTarget(): List<CustomEnchantTargets> {
        return allowedItems
    }

    override fun getItemTarget(): EnchantmentTarget {
        TODO("Not yet implemented")
    }

    override fun isTradeable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDiscoverable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getDamageIncrease(level: Int, entityCategory: EntityCategory): Float {
        TODO("Not yet implemented")
    }

    override fun getActiveSlots(): MutableSet<EquipmentSlot> {
        TODO("Not yet implemented")
    }

    override fun isTreasure(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCursed(): Boolean {
        TODO("Not yet implemented")
    }

}

enum class CustomEnchantTargets {

    ALL {
        override fun includes(item: Material): Boolean {
            return values().firstNotNullOfOrNull { it.includes(item) && it != this } == true
        }
    },

    SWORD {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_SWORD
                    || item == Material.STONE_SWORD
                    || item == Material.IRON_SWORD
                    || item == Material.GOLDEN_SWORD
                    || item == Material.DIAMOND_SWORD
                    || item == Material.NETHERITE_SWORD
        }
    },

    AXE {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_AXE
                    || item == Material.STONE_AXE
                    || item == Material.IRON_AXE
                    || item == Material.GOLDEN_AXE
                    || item == Material.DIAMOND_AXE
                    || item == Material.NETHERITE_AXE
        }
    },

    HOE {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_HOE
                    || item == Material.STONE_HOE
                    || item == Material.IRON_HOE
                    || item == Material.GOLDEN_HOE
                    || item == Material.DIAMOND_HOE
                    || item == Material.NETHERITE_HOE
        }
    },

    SHOVEL {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_SHOVEL
                    || item == Material.STONE_SHOVEL
                    || item == Material.IRON_SHOVEL
                    || item == Material.DIAMOND_SHOVEL
                    || item == Material.GOLDEN_SHOVEL
                    || item == Material.NETHERITE_SHOVEL
        }
    },

    PICKAXE {
        override fun includes(item: Material): Boolean {
            return item == Material.WOODEN_PICKAXE
                    || item == Material.STONE_PICKAXE
                    || item == Material.IRON_PICKAXE
                    || item == Material.DIAMOND_PICKAXE
                    || item == Material.GOLDEN_PICKAXE
                    || item == Material.NETHERITE_PICKAXE
        }
    },

    ARMOR {
        override fun includes(item: Material): Boolean {
            return ARMOR_FEET.includes(item)
                    || ARMOR_LEGS.includes(item)
                    || ARMOR_HEAD.includes(item)
                    || ARMOR_TORSO.includes(item)
        }
    },

    /**
     * Allows the Enchantment to be placed on feet slot armor
     */
    ARMOR_FEET {
        override fun includes(item: Material): Boolean {
            return item == Material.LEATHER_BOOTS
                    || item == Material.CHAINMAIL_BOOTS
                    || item == Material.IRON_BOOTS
                    || item == Material.DIAMOND_BOOTS
                    || item == Material.GOLDEN_BOOTS
                    || item == Material.NETHERITE_BOOTS
        }
    },

    /**
     * Allows the Enchantment to be placed on leg slot armor
     */
    ARMOR_LEGS {
        override fun includes(item: Material): Boolean {
            return item == Material.LEATHER_LEGGINGS
                    || item == Material.CHAINMAIL_LEGGINGS
                    || item == Material.IRON_LEGGINGS
                    || item == Material.DIAMOND_LEGGINGS
                    || item == Material.GOLDEN_LEGGINGS
                    || item == Material.NETHERITE_LEGGINGS
        }
    },

    /**
     * Allows the Enchantment to be placed on torso slot armor
     */
    ARMOR_TORSO {
        override fun includes(item: Material): Boolean {
            return item == Material.LEATHER_CHESTPLATE
                    || item == Material.CHAINMAIL_CHESTPLATE
                    || item == Material.IRON_CHESTPLATE
                    || item == Material.DIAMOND_CHESTPLATE
                    || item == Material.GOLDEN_CHESTPLATE
                    || item == Material.NETHERITE_CHESTPLATE
        }
    },

    /**
     * Allows the Enchantment to be placed on head slot armor
     */
    ARMOR_HEAD {
        override fun includes(item: Material): Boolean {
            return item == Material.LEATHER_HELMET
                    || item == Material.CHAINMAIL_HELMET
                    || item == Material.DIAMOND_HELMET
                    || item == Material.IRON_HELMET
                    || item == Material.GOLDEN_HELMET
                    || item == Material.TURTLE_HELMET
                    || item == Material.NETHERITE_HELMET
        }
    },

    TOOL {
        override fun includes(item: Material): Boolean {
            return SWORD.includes(item)
                    || AXE.includes(item)
                    || SHOVEL.includes(item)
                    || HOE.includes(item)
                    || PICKAXE.includes(item)
        }
    },

    BOW {
        override fun includes(item: Material): Boolean {
            return item == Material.BOW
        }
    },

    CROSSBOW {
        override fun includes(item: Material): Boolean {
            return item == Material.CROSSBOW
        }
    },

    FISHING_ROD {
        override fun includes(item: Material): Boolean {
            return item == Material.FISHING_ROD
        }
    },

    BREAKABLE {
        override fun includes(item: Material): Boolean {
            return item.maxDurability > 0 && item.maxStackSize == 1
        }
    },

    TRIDENT {
        override fun includes(item: Material): Boolean {
            return item == Material.TRIDENT
        }
    },

    WEARABLE {
        override fun includes(item: Material): Boolean {
            return (EnchantmentTarget.ARMOR.includes(item) ||
                    item == Material.ELYTRA ||
                    item == Material.CARVED_PUMPKIN ||
                    item == Material.JACK_O_LANTERN ||
                    item == Material.SKELETON_SKULL ||
                    item == Material.WITHER_SKELETON_SKULL ||
                    item == Material.ZOMBIE_HEAD ||
                    item == Material.PLAYER_HEAD ||
                    item == Material.CREEPER_HEAD ||
                    item == Material.DRAGON_HEAD)
        }
    },

    VANISHABLE {
        override fun includes(item: Material): Boolean {
            return EnchantmentTarget.BREAKABLE.includes(item)
                    || EnchantmentTarget.WEARABLE.includes(item) && item != Material.ELYTRA
                    || item == Material.COMPASS
        }
    };


    /**
     * Check whether this target includes the specified item.
     *
     * @param item The item to check
     * @return True if the target includes the item
     */
    abstract fun includes(item: Material): Boolean

    /**
     * Check whether this target includes the specified item.
     *
     * @param item The item to check
     * @return True if the target includes the item
     */
    fun includes(item: ItemStack): Boolean {
        return includes(item.type)
    }

}
