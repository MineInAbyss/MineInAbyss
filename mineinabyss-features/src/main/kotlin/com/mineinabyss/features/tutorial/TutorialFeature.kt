package com.mineinabyss.features.tutorial

import com.mineinabyss.components.tutorial.TutorialEntity
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.TextDisplay

private val defaultTutorialEntities: List<TutorialEntity> = listOf(
    TutorialEntity(location = Location(Bukkit.getWorld("world"), -576.0, 98.0, -76.0),
        text = "<yellow>Ores generate as you mine,\n and different <gold>Layers <yellow>have different ores and %.\n<yellow>You can get a list of spawns from <gold>/orespawns"),

    TutorialEntity(location = Location(Bukkit.getWorld("world"), -559.0, 130.0, -39.0),
        text = "<light_purple>Follow the purple arrows"),

    TutorialEntity(location = Location(Bukkit.getWorld("world"), -575.0, 99.0, -120.0),
        text = "<red>Bonfires are used to set Respawn Points\n<red>You can find the recipe for them in\n<red>your recipe book in <dark_red>Crafting Tables\n<red>To set a spawnpoint do <dark_red>Crouch + Right Click"),

    TutorialEntity(location = Location(Bukkit.getWorld("world"), -572.0, 98.0, -91.0),
        text = "<gold>Right-clicking <yellow>crops will harvest and replant it\n<gold>Sickles <yellow>are tools used to harvest <yellow>large areas at ones.\n<yellow>Recipe is found in your recipe book"),

    TutorialEntity(location = Location(Bukkit.getWorld("world"), -567.0, 97.0, -72.0),
        text = "<green>All custom recipes can be found\n<green>within the <dark_green>Recipe Book <green>in your\n<green>inventory, crafting table and furnaces"),

    TutorialEntity(location = Location(Bukkit.getWorld("world"), -560.0, 99.0, -107.0),
        text = "<green>Emotes can be used by typing <dark_green><bold>:emotename: :siggy:\n<green>You can find all the emotes in <dark_green><bold>/emote list\n<gold>Patreon <yellow>and <gold>Ko-Fi <yellow>supporters have access\n<yellow>to using GIFs like <white>:gurawave:"),

    TutorialEntity(location = Location(Bukkit.getWorld("world"), -571.0, 99.0, -146.0),
        text = "<dark_aqua>You are on your own now!\n<dark_aqua>Got to find or make your own"),

    TutorialEntity(location = Location(Bukkit.getWorld("world"), -569.0, 96.0, -63.0),
        text = "<aqua>Water, like every other block,\n<aqua> does not negate falldamage.\n<dark_aqua>Swimming down or walking under\n<dark_aqua>waterfalls will damage you.")
)

@Serializable
@SerialName("tutorial")
class TutorialFeature(private val tutorialTexts: List<TutorialEntity> = defaultTutorialEntities) : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {

        for (entity in tutorialTexts) {
            val tutorialDisplay = Bukkit.getWorld("world")?.let { it.getNearbyEntitiesByType(TextDisplay::class.java, entity.location, 0.1).firstOrNull() ?: it.spawn(entity.location, TextDisplay::class.java) } ?: return

            tutorialDisplay.text(entity.text.miniMsg())
            tutorialDisplay.billboard = entity.billboard
            tutorialDisplay.alignment = entity.alignment
            tutorialDisplay.isShadowed = entity.shadow
            tutorialDisplay.backgroundColor = entity.backgroundColor
        }
    }
}
