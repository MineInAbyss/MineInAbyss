package com.derongan.minecraft.mineinabyss

import com.derongan.minecraft.guiy.GuiListener
import com.derongan.minecraft.mineinabyss.ascension.AscensionListener
import com.derongan.minecraft.mineinabyss.commands.AscensionCommandExecutor
import com.derongan.minecraft.mineinabyss.commands.GUICommandExecutor
import com.derongan.minecraft.mineinabyss.geary.AbyssLocationSystem
import com.derongan.minecraft.mineinabyss.geary.DepthMeter
import com.derongan.minecraft.mineinabyss.player.PlayerListener
import com.google.common.collect.ImmutableSet
import com.mineinabyss.geary.GearyService
import com.mineinabyss.geary.ecs.component.components.equipment.Durability
import com.mineinabyss.geary.ecs.component.components.grappling.GrapplingHook
import com.mineinabyss.geary.ecs.component.components.rendering.DisplayState
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

class MineInAbyss : JavaPlugin() {
    private var gearyService: GearyService? = null
    @ExperimentalCommandDSL
    override fun onEnable() {
        // Plugin startup logic
        logger.info("On enable has been called")
        AbyssContext

        //Vault setup
        if (!setupEconomy()) {
            logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", description.name))
            server.pluginManager.disablePlugin(this)
            return
        }

        //Geary setup
        if (setupGeary()) {
            val grapplingRecipeKey = NamespacedKey(this, "grappling_hook")
            val starCompassRecipeKey = NamespacedKey(this, "star_compas")
            val recipeIterator = server.recipeIterator()

            // Remove recipes if already loaded. This way changes will take effect properly.
            // TODO change to config based (in geary or mine in abyss) instead of manual.
            while (recipeIterator.hasNext()) {
                val r = recipeIterator.next()
                if (r is ShapedRecipe) {
                    if (ImmutableSet.of(grapplingRecipeKey, starCompassRecipeKey)
                                    .contains(r.key)) {
                        recipeIterator.remove()
                    }
                }
            }
            server.addRecipe(getGrapplingHookRecipe(grapplingRecipeKey))
            server.addRecipe(getStarCompassRecipe(starCompassRecipeKey))
            gearyService!!.addSystem(AbyssLocationSystem(AbyssContext), this)
        } else {
            logger.warning(String.format("[%s] - Geary service not found! No items have been added!",
                    description.name))
        }
        server.pluginManager.registerEvents(GuiListener(this), this)
        server.pluginManager.registerEvents(PlayerListener(AbyssContext), this)
        server.pluginManager.registerEvents(AscensionListener(), this)

        //register command executors
        AscensionCommandExecutor()
        GUICommandExecutor()
    }

    private fun getGrapplingHookRecipe(grapplingRecipeKey: NamespacedKey): ShapedRecipe {
        val itemStack = ItemStack(Material.DIAMOND_SHOVEL)
        val itemMeta = itemStack.itemMeta
        itemMeta!!.setDisplayName("Grappling Hook")
        itemMeta.setCustomModelData(3)
        itemStack.itemMeta = itemMeta
        gearyService!!.attachToItemStack(ImmutableSet
                .of(GrapplingHook(1.3, 3, 4, Color.fromRGB(142, 89, 60), 1),
                        Durability(64), DisplayState(3)), itemStack)
        val recipe = ShapedRecipe(grapplingRecipeKey, itemStack)
        recipe.shape("III", "ISI", " S ")
        recipe.setIngredient('I', Material.IRON_INGOT)
        recipe.setIngredient('S', Material.STRING)
        return recipe
    }

    private fun getStarCompassRecipe(starCompassRecipeKey: NamespacedKey): ShapedRecipe {
        val itemStack = ItemStack(Material.COMPASS)
        val itemMeta = itemStack.itemMeta
        itemMeta!!.setDisplayName("Star Compass")
        itemStack.itemMeta = itemMeta
        gearyService!!.attachToItemStack(ImmutableSet.of(DepthMeter(250), DisplayState(1)), itemStack)
        val recipe = ShapedRecipe(starCompassRecipeKey, itemStack)
        recipe.shape(" I ", "IPI", " I ")
        recipe.setIngredient('I', Material.GLASS)
        recipe.setIngredient('P', Material.PRISMARINE_CRYSTALS)
        return recipe
    }

    override fun onDisable() {
        // Plugin shutdown logic
        AbyssContext.configManager.saveAll()
        logger.info("onDisable has been invoked!")
    }

    //economy stuff
    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        econ = rsp.provider
        return econ != null
    }

    private fun setupGeary(): Boolean {
        if (server.pluginManager.isPluginEnabled("Geary")) {
            val rsp = server.servicesManager.getRegistration(GearyService::class.java) ?: return false
            gearyService = rsp.provider
            return gearyService != null
        }
        return false
    }

    companion object {
        @JvmStatic
        var econ: Economy? = null
            private set
        @JvmStatic
        val instance: MineInAbyss?
            get() {
                val plugin = Bukkit.getServer().pluginManager.getPlugin("MineInAbyss")
                return plugin as MineInAbyss?
            }
    }
}