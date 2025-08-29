plugins {
    alias(idofrontLibs.plugins.mia.kotlin.jvm)
    alias(idofrontLibs.plugins.kotlinx.serialization)
    alias(idofrontLibs.plugins.mia.copyjar)
    alias(idofrontLibs.plugins.mia.nms)
    alias(idofrontLibs.plugins.mia.publication)
    alias(idofrontLibs.plugins.mia.autoversion)
    alias(idofrontLibs.plugins.mia.papermc)
}

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.mineinabyss.com/mirror")
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenLocal()
    }

    dependencies {
        val idofrontLibs = rootProject.idofrontLibs
        val libs = rootProject.libs

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
    }
}

dependencies {
    // Shaded
    api(project(":mineinabyss-features"))
    api(project(":mineinabyss-components"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlin.ExperimentalUnsignedTypes",
            "-Xcontext-receivers"
        )
    }
}
