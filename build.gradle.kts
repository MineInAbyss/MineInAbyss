import java.net.URI

plugins {
    alias(libs.plugins.mia.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.mia.copyjar)
    alias(libs.plugins.mia.publication)
    alias(libs.plugins.mia.autoversion)
    alias(libs.plugins.mia.papermc)
}

val mavenUser: String by project
val mavenPassword: String by project
allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.mineinabyss.com/mirror")
        maven {
            url = URI("https://${mavenUser}:${mavenPassword}@repo.mineinabyss.com/private")
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.dmulloy2.net/nexus/repository/public/") //ProtocolLib
//        maven("https://m2.dv8tion.net/releases") // DiscordSRV
        maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        mavenLocal()
        maven("https://ci.mg-dev.eu/plugin/repository/everything") // TrainCarts
        //maven("https://repo.skyslycer.de/releases/") // HMCWraps
        maven("https://nexus.lichtspiele.org/repository/releases/") // Shopkeepers
        maven("https://jitpack.io")
    }

    dependencies {
        val libs = rootProject.libs
        val miaLibs = rootProject.miaLibs

        // Shaded
        implementation(libs.bundles.idofront.core)

        // Geary platform
        compileOnly(miaLibs.geary.papermc)

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

        compileOnly(miaLibs.guiy)
        compileOnly(miaLibs.chatty)
        compileOnly(miaLibs.deeperworld)
        compileOnly(miaLibs.mobzy)
        compileOnly(miaLibs.looty) { isChanging = true }
        compileOnly(miaLibs.minecraft.plugin.modelengine)
        compileOnly(miaLibs.minecraft.plugin.blocklocker)
        compileOnly(miaLibs.minecraft.plugin.gsit)
        compileOnly(miaLibs.minecraft.plugin.hmccosmetics)
        compileOnly(miaLibs.minecraft.plugin.discordsrv)
        compileOnly(miaLibs.minecraft.plugin.luckperms)
        compileOnly(miaLibs.minecraft.plugin.placeholderapi)
        compileOnly(miaLibs.minecraft.plugin.happyhud)
        compileOnly(miaLibs.minecraft.plugin.bkcommonlib)
        compileOnly(miaLibs.minecraft.plugin.traincarts)
        compileOnly(miaLibs.minecraft.plugin.tccoasters)
        compileOnly(miaLibs.minecraft.plugin.shopkeepers)
        compileOnly(miaLibs.minecraft.plugin.openinv)
    }
}

dependencies {
    // Shaded
    implementation(project(":mineinabyss-core"))
    implementation(project(":mineinabyss-features"))
}
