import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder.BEFORE
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(miaLibs.plugins.kotlin.jvm)
    alias(miaLibs.plugins.mia.papermc)
    alias(miaLibs.plugins.mia.nms)
    alias(miaLibs.plugins.mia.copyjar)
    alias(miaLibs.plugins.compose.compiler)
    alias(miaLibs.plugins.mia.publication)
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":mineinabyss-components"))

    // Shaded
    compileOnly(miaLibs.idofront.nms)

    // Geary platform
    compileOnly(miaLibs.geary.papermc)

    // MineInAbyss platform
    implementation(miaLibs.exposed.core) { isTransitive = false }
    implementation(miaLibs.exposed.dao) { isTransitive = false }
    implementation(miaLibs.exposed.jdbc) { isTransitive = false }
    implementation(miaLibs.exposed.javatime) { isTransitive = false }

    compileOnly(miaLibs.bundles.idofront.core)
    compileOnly(miaLibs.kotlin.stdlib)
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.kotlinx.coroutines)
    compileOnly(miaLibs.minecraft.mccoroutine)
    compileOnly(miaLibs.reflections)
    compileOnly(miaLibs.sqlite.jdbc)
    compileOnly(miaLibs.sqlite.kt)

    // Plugin libs
    compileOnly(libs.deeperworld)

    compileOnly(miaLibs.minecraft.plugin.modelengine)
    compileOnly(miaLibs.guiy)
    compileOnly(miaLibs.chatty)
    compileOnly(miaLibs.packy)
    compileOnly(miaLibs.extracommands)
    compileOnly(miaLibs.minecraft.plugin.blocklocker)
    compileOnly(miaLibs.minecraft.plugin.hmccosmetics)
    compileOnly(miaLibs.minecraft.plugin.hibiscuscommons)
    compileOnly(miaLibs.minecraft.plugin.discordsrv)
    compileOnly(miaLibs.minecraft.plugin.luckperms)
    compileOnly(miaLibs.minecraft.plugin.placeholderapi)
    compileOnly(miaLibs.minecraft.plugin.bkcommonlib)
    compileOnly(miaLibs.minecraft.plugin.traincarts)
    compileOnly(miaLibs.minecraft.plugin.tccoasters)
    compileOnly(miaLibs.minecraft.plugin.mythichud)
    compileOnly(miaLibs.minecraft.plugin.shopkeepers)
    compileOnly(miaLibs.minecraft.plugin.luxdialogs)

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi",
            "-opt-in=kotlin.ExperimentalUnsignedTypes",
            "-Xcontext-parameters",
        )
    }
}
val compileKotlin: KotlinCompile by tasks


paper {
    name = "MineInAbyss"
    main = "com.mineinabyss.features.MineInAbyssPlugin"
    authors = listOf("Derongan", "Offz", "Boy0000")
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    prefix = "MiA"
    description = "The core plugin for Mine in Abyss features"

    permissions {
        register("mineinabyss.stopdescent") {
            description = "Allows players to leave a run"
        }
        register("mineinabyss.start") {
            description = "Allows players to start a run"
        }
        register("mineinabyss.stats") {
            description = "Allows players to see their stats"
        }
    }

    serverDependencies {
        register("Idofront") {
            required = true
            load = BEFORE
            joinClasspath = true
        }
        register("Geary") {
            required = true
            load = BEFORE
            joinClasspath = true
        }
        register("Guiy") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("DeeperWorld") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("ExtraCommands") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("LuxDialogues") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("Vault") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("Multiverse-Core") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("HMCCosmetics") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("DiscordSRV") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("Chatty") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("ModelEngine") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("Train_Carts") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("TCCoasters") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("LuckPerms") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("MythicHUD") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("BlockLocker") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("Packy") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("Blocky") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("PlaceholderAPI") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("Shopkeepers") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
    }
}