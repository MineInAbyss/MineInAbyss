plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    kotlin("jvm") apply false
}

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        //mavenLocal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://mvn.lumine.io/repository/maven-public/") { metadataSources { artifact() } } // Model Engine
        maven("https://repo.dmulloy2.net/nexus/repository/public/") //ProtocolLib
        maven("https://mvn.lumine.io/repository/maven-public/") // MCCosmetics
        maven("https://m2.dv8tion.net/releases") // DiscordSRV
        maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.skyslycer.de/releases/") // HMCWraps
        maven("https://nexus.lichtspiele.org/repository/releases/") // Shopkeepers
        maven("https://jitpack.io")
    }

    dependencies {
        val libs = rootProject.libs
        val miaLibs = rootProject.miaLibs

        // Shaded
        implementation(libs.idofront.core)
        implementation(libs.idofront.autoscan) {
            exclude("org.reflections")
        }

        // Geary platform
        compileOnly(miaLibs.geary.papermc.core)
        compileOnly(miaLibs.geary.commons.papermc)

        // MineInAbyss platform
        compileOnly(libs.kotlin.stdlib)
        compileOnly(libs.kotlinx.serialization.json)
        compileOnly(libs.kotlinx.serialization.kaml)
        compileOnly(libs.kotlinx.coroutines)
        compileOnly(libs.minecraft.mccoroutine)
        compileOnly(libs.reflections)
        compileOnly(libs.exposed.core) { isTransitive = false }
        compileOnly(libs.exposed.dao) { isTransitive = false }
        compileOnly(libs.exposed.jdbc) { isTransitive = false }
        compileOnly(libs.exposed.javatime) { isTransitive = false }
        compileOnly(libs.sqlite.jdbc)
        compileOnly(libs.minecraft.anvilgui)

        // Plugin libs
        compileOnly(libs.minecraft.plugin.vault) { exclude(group = "org.bukkit") }
        compileOnly(libs.minecraft.plugin.fawe.core)
        compileOnly(libs.minecraft.plugin.fawe.bukkit) { isTransitive = false }
        compileOnly(libs.minecraft.plugin.protocollib)

        compileOnly(miaLibs.looty)
        compileOnly(miaLibs.mobzy)
        compileOnly(miaLibs.chatty)
        compileOnly(miaLibs.deeperworld)
        compileOnly(miaLibs.minecraft.plugin.modelengine)
        compileOnly(miaLibs.minecraft.plugin.blocklocker)
        compileOnly(miaLibs.minecraft.plugin.gsit)
        compileOnly(miaLibs.minecraft.plugin.mccosmetics)
        compileOnly(miaLibs.minecraft.plugin.hmccosmetics)
        compileOnly(miaLibs.minecraft.plugin.discordsrv)
        compileOnly(miaLibs.minecraft.plugin.luckperms)
        compileOnly(miaLibs.minecraft.plugin.placeholderapi)
        compileOnly(miaLibs.minecraft.plugin.happyhud)
        compileOnly(miaLibs.minecraft.plugin.hmcwraps)
        compileOnly(miaLibs.minecraft.plugin.shopkeepers)
    }
}

dependencies {
    // Shaded
    implementation(project(":mineinabyss-core"))
    implementation(project(":mineinabyss-features"))
}
