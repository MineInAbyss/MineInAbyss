package com.mineinabyss.features.tutorial

import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.TextDisplay
import org.joml.Vector3f

@Serializable
@SerialName("tutorial")
class TutorialFeature : AbyssFeature {
    private fun spawnTutorialEntities() {
        for (entity in tutorial.tutorialEntities) {
            orth.spawn(entity.location, TextDisplay::class.java) { textDisplay ->
                textDisplay.text(entity.text.miniMsg())
                textDisplay.billboard = entity.billboard
                textDisplay.alignment = entity.alignment
                textDisplay.isShadowed = entity.shadow
                textDisplay.backgroundColor = entity.backgroundColor
                entity.viewRange?.let { textDisplay.viewRange = it }
                textDisplay.transformation = textDisplay.transformation.apply { scale.set(entity.scale) }

                textDisplay.isPersistent = false
            }
        }
    }

    private fun setTutorialContext() {
        DI.remove<TutorialContext>()
        DI.add<TutorialContext>(object : TutorialContext {
            override val tutorialEntities: List<TutorialEntity> by config("tutorialEntities") { abyss.plugin.fromPluginPath(loadDefault = true) }
        })
    }

    override fun MineInAbyssPlugin.enableFeature() {

        setTutorialContext()
        spawnTutorialEntities()

        commands {
            mineinabyss {
                "tutorial"(desc = "Opens the tutorial") {
                    "spawn" {
                        action {
                            spawnTutorialEntities()
                        }
                    }
                    "reload" {
                        action {
                            setTutorialContext()
                            spawnTutorialEntities()
                            sender.success("Tutorial reloaded")
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("tutorial").filter { it.startsWith(args[0]) }
                    else -> emptyList()
                }
            }
        }

    }
}

val orth = Bukkit.getWorld("world")!!
private val defaultTutorialEntities: List<TutorialEntity> = listOf(
    // Mining info
    TutorialEntity(
        location = Location(orth, -576.0, 98.0, -76.0),
        text = """
            <yellow>Ores generate as you mine,
             and different <gold>Layers <yellow>have different ores and %.
            <yellow>You can get a list of spawns from <gold>/orespawns
        """.trimIndent()
    ),

    // Tutorial beginning
    TutorialEntity(location = Location(orth, -559.0, 130.0, -39.0),
        text = "<light_purple>Follow the purple arrows"),

    // Bonfire info
    TutorialEntity(location = Location(orth, -575.0, 99.0, -120.0),
        text = """
            <red>Bonfires are used to set Respawn Points
            <red>You can find the recipe for them in
            <red>your recipe book in <dark_red>Crafting Tables
            <red>To set a spawnpoint do <dark_red>Crouch + Right Click
        """.trimIndent()),

    // Crops info
    TutorialEntity(location = Location(orth, -572.0, 98.0, -91.0),
        text = """
            <gold>Right-clicking <yellow>crops will harvest and replant it
            <gold>Sickles <yellow>are tools used to harvest <yellow>large areas at ones.
            <yellow>Recipe is found in your recipe book
        """.trimIndent()),

    // Custom recipes info
    TutorialEntity(location = Location(orth, -567.0, 97.0, -72.0),
        text = """
            <green>All custom recipes can be found
            <green>within the <dark_green>Recipe Book <green>in your
            <green>inventory, crafting table and furnaces
        """.trimIndent()),

    // Emotes info
    TutorialEntity(location = Location(orth, -560.0, 99.0, -107.0),
        text = """
            <green>Emotes can be used by typing <dark_green><bold>:emotename: :siggy:
            <green>You can find all the emotes in <dark_green><bold>/emote list
            <gold>Patreon <yellow>and <gold>Ko-Fi <yellow>supporters have access
            <yellow>to using GIFs like <white>:gurawave:
        """.trimIndent()),

    // End of tutorial
    TutorialEntity(location = Location(orth, -571.0, 99.0, -146.0),
        text = """
            <dark_aqua>You are on your own now!
            <dark_aqua>Got to find or make your own
        """.trimIndent()),

    // Water info
    TutorialEntity(location = Location(orth, -569.0, 96.0, -63.0),
        text = """
            <aqua>Water, like every other block,
            <aqua> does not negate falldamage.
            <dark_aqua>Swimming down or walking under
            <dark_aqua>waterfalls will damage you.
        """.trimIndent()),

    // Welcome
    TutorialEntity(location = Location(orth, -556.0, 129.0, -30.0),
        text = """
            <yellow>Welcome to <gradient:#ff7043:#ffca28:#ff7043>Mine In Abyss!
            <yellow>Here are some tips to get you started.
            
            
           
            Do <aqua>/discord</aqua> to get an invite to our Discord
            Visit <light_purple>#information</light_purple> for general server info
        """.trimIndent()),

    // General information
    TutorialEntity(location = Location(orth, -557.0, 129.0, -34.0), scale = Vector3f(0.6f, 0.6f, 0.6f),
        alignment = TextDisplay.TextAlignment.LEFT, text = """
                                <gray>General Info</gray>
            <gray>*</gray> The abyss goes down to Layer 5
            <gray>*</gray> The curse exists in the Abyss
            <gray>*</gray> The rank you have is determined by what layer you are on
            <gray>*</gray> Going down water will damage you
            <gray>*</gray> There is no way to negate fall damage
            <gray>*</gray> Layer 6 is not in development yet
            <gray>*</gray> Server is anime-only, discussing manga-content is not allowed
            <gray>*</gray> Ores spawn in as you mine, do not expect to see any in walls
            <gray>*</gray> You can climb a wall by right clicking it.
            <gray>*</gray> Holding Space whilst climbing will make you climb up
            <gray>*</gray> Holding Sneak whilst climbing will make you climb down
        """.trimIndent()),

    // Rules
    TutorialEntity(location = Location(orth, -557.0, 129.0, -26.5), scale = Vector3f(0.5f, 0.5f, 0.5f),
        alignment = TextDisplay.TextAlignment.LEFT, text = """
                                    <red>Rules</red>
            1. <#e91e63>No griefing of other people's builds</#e91e63>
            2. <#e91e63>Hacks that give the player an unfair advantage are not allowed</#e91e63>
            3. <#e91e63>Be nice and dont annoy people</#e91e63>
            4. <#e91e63>No spamming the chat. Arguments should happen outside of global chat</#e91e63>
            5. <#e91e63>Laddershafts, rails and staircases are <b>not allowed</b> above Layer 4</#e91e63>
            6. <#e91e63>You are not allowed to take advantage of any bug or exploit you may come across.</#e91e63>
        """.trimIndent()),
)

