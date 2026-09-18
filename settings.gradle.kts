rootProject.name = "mineinabyss"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
        mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    val miaLibs: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }

    versionCatalogs {
        create("miaLibs") {
            from("com.mineinabyss:catalog:$miaLibs")
            version("minecraft-server", "26.3.build.18-alpha")
            version("java", "25")
            version("kotlin", "2.4.20")
            version("creative", "1.15.1")
            version("idofront", "2.0")
            version("gearyPaper", "0.34")
            version("chatty", "0.10")
            version("extracommands", "0.14")
            version("minecraft-plugin-modelengine", "R4.1.1")
            version("minecraft-plugin-mythic-dist", "5.13.1-SNAPSHOT")
            version("sqlite-kt", "0.1.4-dev.1")
        }
    }
}

include(
    "mineinabyss-components",
    "mineinabyss-features"
)

//includeBuild("../DeeperWorld")