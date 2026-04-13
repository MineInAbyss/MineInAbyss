import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(idofrontLibs.plugins.kotlin.jvm)
    alias(idofrontLibs.plugins.mia.papermc)
    alias(idofrontLibs.plugins.mia.nms)
    alias(idofrontLibs.plugins.mia.copyjar)
    alias(idofrontLibs.plugins.compose.compiler)
    alias(idofrontLibs.plugins.mia.publication)
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":mineinabyss-components"))

    // Shaded
    compileOnly(idofrontLibs.idofront.features)
    compileOnly(idofrontLibs.idofront.nms)

    // Geary platform
    compileOnly(libs.geary.papermc)

    // MineInAbyss platform
    implementation(idofrontLibs.exposed.core) { isTransitive = false }
    implementation(idofrontLibs.exposed.dao) { isTransitive = false }
    implementation(idofrontLibs.exposed.jdbc) { isTransitive = false }
    implementation(idofrontLibs.exposed.javatime) { isTransitive = false }

    compileOnly(idofrontLibs.bundles.idofront.core)
    compileOnly(idofrontLibs.kotlin.stdlib)
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.kotlinx.coroutines)
    compileOnly(idofrontLibs.minecraft.mccoroutine)
    compileOnly(idofrontLibs.reflections)
    compileOnly(idofrontLibs.sqlite.jdbc)

    // Plugin libs
    compileOnly(idofrontLibs.minecraft.plugin.modelengine)

    compileOnly(libs.guiy)
    compileOnly(libs.chatty)
    compileOnly(libs.deeperworld)
    compileOnly(libs.packy)
    compileOnly(libs.extracommands)

    compileOnly(libs.minecraft.plugin.blocklocker)
    compileOnly(libs.minecraft.plugin.hmccosmetics)
    compileOnly(libs.minecraft.plugin.hibiscuscommons)
    compileOnly(libs.minecraft.plugin.discordsrv)
    compileOnly(libs.minecraft.plugin.luckperms)
    compileOnly(libs.minecraft.plugin.placeholderapi)
    compileOnly(libs.minecraft.plugin.bkcommonlib)
    compileOnly(libs.minecraft.plugin.traincarts)
    compileOnly(libs.minecraft.plugin.tccoasters)
    compileOnly(libs.minecraft.plugin.mythichud)
    compileOnly(libs.minecraft.plugin.shopkeepers)
    compileOnly(libs.minecraft.plugin.luxdialogs)

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi",
            "-opt-in=kotlin.ExperimentalUnsignedTypes",
            "-Xcontext-receivers",
        )
    }
}
val compileKotlin: KotlinCompile by tasks
