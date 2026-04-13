plugins {
    alias(idofrontLibs.plugins.kotlin.jvm) apply false
    alias(idofrontLibs.plugins.kotlinx.serialization) apply false
    alias(idofrontLibs.plugins.mia.papermc) apply false
    alias(idofrontLibs.plugins.mia.nms) apply false
    alias(idofrontLibs.plugins.mia.copyjar) apply false
    alias(idofrontLibs.plugins.compose.compiler) apply false
    alias(idofrontLibs.plugins.mia.publication) apply false
    alias(idofrontLibs.plugins.mia.autoversion)
    alias(idofrontLibs.plugins.mia.docs)
}
//
allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.mineinabyss.com/mirror")
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenLocal()

        // LuxDialogues
        maven("https://repo.aselstudios.com/releases")
    }
}

idofront {
    docsVersion = "0.0.8"
}