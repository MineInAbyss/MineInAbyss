plugins {
    alias(miaLibs.plugins.kotlin.jvm) apply false
    alias(miaLibs.plugins.kotlinx.serialization) apply false
    alias(miaLibs.plugins.mia.papermc) apply false
    alias(miaLibs.plugins.mia.nms) apply false
    alias(miaLibs.plugins.mia.copyjar) apply false
    alias(miaLibs.plugins.compose.compiler) apply false
    alias(miaLibs.plugins.mia.publication) apply false
    alias(miaLibs.plugins.mia.autoversion)
    alias(miaLibs.plugins.mia.docs)
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://repo.mineinabyss.com/mirror")
        maven("https://repo.papermc.io/repository/maven-public/")
        google()
        mavenLocal()
    }
}
