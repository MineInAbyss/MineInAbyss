plugins {
    alias(idofrontLibs.plugins.mia.kotlin.jvm)
    alias(idofrontLibs.plugins.kotlinx.serialization)
    alias(idofrontLibs.plugins.mia.copyjar)
    alias(idofrontLibs.plugins.mia.nms)
    alias(idofrontLibs.plugins.mia.publication)
    alias(idofrontLibs.plugins.mia.autoversion)
    alias(idofrontLibs.plugins.mia.papermc)
}

val mavenUser = if (project.hasProperty("mavenUser")) project.property("mavenUser") as String else System.getenv("MAVEN_USERNAME") ?: ""
val mavenPassword = if (project.hasProperty("mavenPassword")) project.property("mavenPassword") as String else System.getenv("MAVEN_PASSWORD") ?: ""
allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.hibiscusmc.com/releases/")
        maven("https://repo.mineinabyss.com/mirror")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.spaceio.xyz/repository/maven-public/")
        maven("https://mvn.lumine.io/repository/maven-public/")
//        maven("https://m2.dv8tion.net/releases") // DiscordSRV
        maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://ci.mg-dev.eu/plugin/repository/everything") // TrainCarts
        maven("https://jitpack.io")
        mavenLocal()
    }

    dependencies {
        val idofrontLibs = rootProject.idofrontLibs
        val miaLibs = rootProject.miaLibs

        // Shaded
        compileOnly(idofrontLibs.idofront.features)
        compileOnly(idofrontLibs.idofront.nms)

        // Geary platform
        compileOnly(miaLibs.geary.papermc)

        // MineInAbyss platform
        compileOnly(idofrontLibs.bundles.idofront.core)
        compileOnly(idofrontLibs.kotlin.stdlib)
        compileOnly(idofrontLibs.kotlinx.serialization.json)
        compileOnly(idofrontLibs.kotlinx.serialization.kaml)
        compileOnly(idofrontLibs.kotlinx.coroutines)
        compileOnly(idofrontLibs.minecraft.mccoroutine)
        compileOnly(idofrontLibs.reflections)
        compileOnly(idofrontLibs.exposed.core) { isTransitive = false }
        compileOnly(idofrontLibs.exposed.dao) { isTransitive = false }
        compileOnly(idofrontLibs.exposed.jdbc) { isTransitive = false }
        compileOnly(idofrontLibs.exposed.javatime) { isTransitive = false }
        compileOnly(idofrontLibs.sqlite.jdbc)
        compileOnly(idofrontLibs.minecraft.anvilgui)

        // Plugin libs
        compileOnly(idofrontLibs.minecraft.plugin.modelengine)

        compileOnly(miaLibs.guiy)
        compileOnly(miaLibs.chatty)
        compileOnly(miaLibs.deeperworld)
        compileOnly(miaLibs.eternalfortune)
        compileOnly(miaLibs.packy)
        compileOnly(miaLibs.blocky)

        compileOnly(miaLibs.minecraft.plugin.blocklocker)
        compileOnly(miaLibs.minecraft.plugin.hmccosmetics)
        compileOnly(miaLibs.minecraft.plugin.hibiscuscommons)
        compileOnly(miaLibs.minecraft.plugin.discordsrv)
        compileOnly(miaLibs.minecraft.plugin.luckperms)
        compileOnly(miaLibs.minecraft.plugin.placeholderapi)
        compileOnly(miaLibs.minecraft.plugin.bkcommonlib)
        compileOnly(miaLibs.minecraft.plugin.traincarts)
        compileOnly(miaLibs.minecraft.plugin.tccoasters)
        //compileOnly(miaLibs.minecraft.plugin.mythichud)
        compileOnly(miaLibs.minecraft.plugin.betterhud)
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
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}
