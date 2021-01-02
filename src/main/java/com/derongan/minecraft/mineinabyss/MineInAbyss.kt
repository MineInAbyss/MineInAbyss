package com.derongan.minecraft.mineinabyss

import com.derongan.minecraft.guiy.GuiListener
import com.derongan.minecraft.mineinabyss.ascension.AscensionListener
import com.derongan.minecraft.mineinabyss.commands.AscensionCommandExecutor
import com.derongan.minecraft.mineinabyss.commands.GUICommandExecutor
import com.derongan.minecraft.mineinabyss.configuration.PlayerDataConfig
import com.derongan.minecraft.mineinabyss.geary.AbyssLocationSystem
import com.derongan.minecraft.mineinabyss.geary.DepthMeter
import com.derongan.minecraft.mineinabyss.player.PlayerListener
import com.derongan.minecraft.mineinabyss.services.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManagerImpl
import com.google.common.collect.ImmutableSet
import com.mineinabyss.geary.GearyService
import com.mineinabyss.geary.ecs.component.components.equipment.Durability
import com.mineinabyss.geary.ecs.component.components.grappling.GrapplingHook
import com.mineinabyss.geary.ecs.component.components.rendering.DisplayState
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.plugin.getServiceOrNull
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import net.milkbowl.vault.economy.Economy
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

class MineInAbyss : JavaPlugin() {
    private val gearyService = getServiceOrNull<GearyService>("Geary")

    @ExperimentalCommandDSL
    override fun onEnable() {
        // Plugin startup logic
        logger.info("On enable has been called")

        AbyssContext
        PlayerDataConfig

        //Vault setup
        if (econ == null) {
            logger.severe("[${description.name}] - Disabled due to no Vault dependency found!")
            server.pluginManager.disablePlugin(this)
            return
        }

        //Geary setup
        if (gearyService != null) {
            val grapplingRecipeKey = NamespacedKey(this, "grappling_hook")
            val starCompassRecipeKey = NamespacedKey(this, "star_compas")
            val recipeIterator = server.recipeIterator()

            // Remove recipes if already loaded. This way changes will take effect properly.
            // TODO change to config based (in geary or mine in abyss) instead of manual.
            recipeIterator.forEachRemaining { r ->
                if (r is ShapedRecipe) {
                    if (setOf(grapplingRecipeKey, starCompassRecipeKey).contains(r.key)) {
                        recipeIterator.remove()
                    }
                }
            }

            server.addRecipe(getGrapplingHookRecipe(grapplingRecipeKey))
            server.addRecipe(getStarCompassRecipe(starCompassRecipeKey))
            gearyService.addSystem(AbyssLocationSystem(), this)
        } else {
            logger.warning("[${description.name}] - Geary service not found! No items have been added!")
        }

        // Remove recipes if already loaded. This way changes will take effect properly.

        registerService<AbyssWorldManager>(AbyssWorldManagerImpl())
        registerEvents(
            GuiListener(this),
            PlayerListener,
            AscensionListener
        )
        //register command executors
        AscensionCommandExecutor
        GUICommandExecutor
    }

    private fun getGrapplingHookRecipe(grapplingRecipeKey: NamespacedKey): ShapedRecipe {
        val itemStack = ItemStack(Material.DIAMOND_SHOVEL).editItemMeta {
            setDisplayName("Grappling Hook")
            setCustomModelData(3)
        }
        gearyService!!.attachToItemStack(
            setOf(
                GrapplingHook(1.3, 3, 4, Color.fromRGB(142, 89, 60), 1),
                Durability(64),
                DisplayState(3)
            ), itemStack
        )

        return ShapedRecipe(grapplingRecipeKey, itemStack).apply {
            shape("III", "ISI", " S ")
            setIngredient('I', Material.IRON_INGOT)
            setIngredient('S', Material.STRING)
        }
    }

    private fun getStarCompassRecipe(starCompassRecipeKey: NamespacedKey): ShapedRecipe {
        val itemStack = ItemStack(Material.COMPASS).editItemMeta {
            setDisplayName("Star Compass")
        }
        gearyService!!.attachToItemStack(ImmutableSet.of(DepthMeter(250), DisplayState(1)), itemStack)
        return ShapedRecipe(starCompassRecipeKey, itemStack).apply {
            shape(" I ", "IPI", " I ")
            setIngredient('I', Material.GLASS)
            setIngredient('P', Material.PRISMARINE_CRYSTALS)
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("onDisable has been invoked!")
        PlayerDataConfig.saveAll()
    }

    companion object {
        @JvmStatic
        val econ by lazy { getServiceOrNull<Economy>("Vault") }
    }
}
